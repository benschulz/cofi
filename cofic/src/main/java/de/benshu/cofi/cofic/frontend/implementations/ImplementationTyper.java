package de.benshu.cofi.cofic.frontend.implementations;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.infer.ExpressionTreeInferencer;
import de.benshu.cofi.cofic.frontend.infer.InferClosure;
import de.benshu.cofi.cofic.frontend.infer.InferFunctionInvocation;
import de.benshu.cofi.cofic.frontend.infer.InferMemberAccess;
import de.benshu.cofi.cofic.frontend.namespace.AbstractResolution;
import de.benshu.cofi.cofic.frontend.namespace.NamespaceTrackingVisitor;
import de.benshu.cofi.cofic.frontend.namespace.ParametersNs;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AbstractionStatement;
import de.benshu.cofi.model.impl.Assignment;
import de.benshu.cofi.model.impl.Closure;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.model.impl.ExpressionStatement;
import de.benshu.cofi.model.impl.FunctionInvocation;
import de.benshu.cofi.model.impl.FunctionInvocationExpression;
import de.benshu.cofi.model.impl.FunctionInvocationStatement;
import de.benshu.cofi.model.impl.LiteralExpression;
import de.benshu.cofi.model.impl.LocalVariableDeclaration;
import de.benshu.cofi.model.impl.MemberAccessExpression;
import de.benshu.cofi.model.impl.NameExpression;
import de.benshu.cofi.model.impl.PropertyDeclaration;
import de.benshu.cofi.model.impl.RootExpression;
import de.benshu.cofi.model.impl.ThisExpr;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.FunctionTypes;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.commons.core.Optional;

import java.util.function.Consumer;

import static de.benshu.cofi.types.impl.lists.AbstractTypeList.typeList;
import static de.benshu.commons.core.Optional.none;
import static java.util.stream.Collectors.joining;

public class ImplementationTyper {
    public static ImplementationData type(Pass pass, ImmutableSet<CompilationUnit<Pass>> compilationUnits) {
        return compilationUnits
                .parallelStream()
                .map(u -> new Visitor(pass).visit(u, ImplementationData.builder()))
                .collect(ImplementationData::builder, ImplementationDataBuilder::addAll, ImplementationDataBuilder::addAll)
                .addAll(new ImplementationDataBuilder(pass.getGenericModelData()))
                .build();
    }

    private static final class Visitor extends NamespaceTrackingVisitor<ImplementationDataBuilder> {
        private final Pass pass;
        private ExpressionTreeInferencer inferencer;

        public Visitor(Pass pass) {
            super(pass);

            this.pass = pass;
            this.inferencer = new ExpressionTreeInferencer(pass);
        }

        @Override
        public ImplementationDataBuilder visitAbstractionStatement(AbstractionStatement<Pass> abstractionStatement, ImplementationDataBuilder aggregate) {
            if (abstractionStatement.pieces.get(0).arguments.isEmpty() || !tryDispatchOnFirstArg(abstractionStatement, aggregate)) {
                throw null;
            }

            TypeSystemImpl<Pass> types = pass.getTypeSystem();
            inferencer.infer(pass, getContextualConstraints(), types.getTop());

            return aggregate;
        }

        private boolean tryDispatchOnFirstArg(AbstractionStatement<Pass> abstractionStatement, ImplementationDataBuilder aggregate) {
            AbstractionStatement.Piece<Pass> piece0 = abstractionStatement.pieces.get(0);

            ExpressionTreeInferencer backup = inferencer;
            inferencer = new ExpressionTreeInferencer(pass);
            visit(piece0.arguments.get(0), aggregate);

            // TODO specific inference
            inferencer.infer(pass, getContextualConstraints(), pass.getTypeSystem().getTop());
            inferencer = backup;

            ProperTypeMixin<Pass, ?> lookUpType = aggregate.lookUpProperTypeOf(piece0.arguments.get(0));

            for (AbstractMember<Pass> _ : lookUpType.lookupMember(abstractionStatement.getName().stream().map(Token::getLexeme).collect(joining()))) {
                dispatchOnFirstArg(abstractionStatement, aggregate);
                return true;
            }
            return false;
        }

        private void dispatchOnFirstArg(final AbstractionStatement<Pass> abstractionStatement, ImplementationDataBuilder aggregate) {
            final AbstractionStatement.Piece<Pass> piece0 = abstractionStatement.pieces.get(0);

            visit(piece0.arguments.get(0), aggregate);

            inferencer.accessMember(new InferMemberAccess() {
                @Override
                public void setTypeArgs(AbstractMember<Pass> member, AbstractTypeList<Pass, ?> typeArgs) {
                    if (!typeArgs.isEmpty())
                        throw null;
//                    abstractionStatement.setTypeArgs(typeArgs);
                }

                @Override
                public Optional<AbstractTypeList<Pass, ?>> getTypeArgs() {
                    return none();
                }

                @Override
                public String getName() {
                    return piece0.name.getLexeme();
                }
            });

            inferencer.beginInvocation(new FunctionInvocationInference(abstractionStatement) {
                @Override
                public int getArgCount() {
                    return piece0.arguments.size() - 1;
                }
            });

            for (int i = 1; i < piece0.arguments.size(); ++i) {
                visit(piece0.arguments.get(i), aggregate);
            }
            visit(piece0.closure, aggregate);

            inferencer.endInvocation();
        }

        @Override
        public ImplementationDataBuilder visitClosure(Closure<Pass> closure, ImplementationDataBuilder aggregate) {
            ExpressionTreeInferencer backup = inferencer;
            inferencer = new ExpressionTreeInferencer(pass);
            for (Closure.Case<Pass> caze : closure.cases) {
                pushNs(ParametersNs.wrap(getNs(), caze.params));
                visitAll(caze.params, aggregate);
                visitStatements(caze.body, aggregate);
                popNs();
            }
            inferencer = backup;

            inferencer.pushClosure(new InferClosure() {
                @Override
                public void setSignature(ProperTypeMixin<Pass, ?> signature) {
                    aggregate.defineTypeOf(closure, signature);
                }

                @Override
                public AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>> getParameterTypes() {
                    return closure.cases.get(0).params.stream()
                            .map(p -> aggregate.lookUpProperTypeOf(p.type))
                            .collect(typeList());
                }
            });

            return aggregate;
        }

        @Override
        public ImplementationDataBuilder visitAssignment(Assignment<Pass> assignment, ImplementationDataBuilder aggregate) {
            visit(assignment.lhs, aggregate);
            // TODO type of rhs must be subtype of type of lhs
            //      ultimately this will be done by syntax transformation
            //      from   lhs := rhs
            //      to     lhs.set(rhs)
            inferencer.infer(pass, getContextualConstraints(), pass.getTypeSystem().getTop());
            visit(assignment.rhs, aggregate);
            // TODO (see above) assignment should be an expression yielding Unit
            inferencer.infer(pass, getContextualConstraints(), pass.getTypeSystem().getTop());

            return aggregate;
        }

        @Override
        public ImplementationDataBuilder visitExpressionStatement(ExpressionStatement<Pass> expressionStatement, ImplementationDataBuilder aggregate) {
            visitAll(expressionStatement.annotations, aggregate);
            expressionStatement.expression.accept(this, aggregate);

            inferencer.infer(pass, getContextualConstraints(), pass.getTypeSystem().getTop());

            return aggregate;
        }

        @Override
        public ImplementationDataBuilder visitFunctionInvocationExpression(FunctionInvocationExpression<Pass> functionInvocationExpression, ImplementationDataBuilder aggregate) {
            visit(functionInvocationExpression.primary, aggregate);

            inferencer.beginInvocation(new FunctionInvocationInference(functionInvocationExpression, s -> {
                final ProperTypeMixin<Pass, ?> returnType = FunctionTypes.extractReturnType(pass, s.getExplicitSignatureType());
                aggregate.defineTypeOf(functionInvocationExpression, returnType);
            }));
            visitAll(functionInvocationExpression.args, aggregate);
            inferencer.endInvocation();

            return aggregate;
        }

        @Override
        public ImplementationDataBuilder visitFunctionInvocationStatement(FunctionInvocationStatement<Pass> functionInvocationStatement, ImplementationDataBuilder aggregate) {
            visitAll(functionInvocationStatement.annotations, aggregate);
            visit(functionInvocationStatement.name, aggregate);
            visitAll(functionInvocationStatement.arguments, aggregate);

            return aggregate;
        }

        @Override
        public ImplementationDataBuilder visitLiteralExpression(LiteralExpression<Pass> literalExpr, ImplementationDataBuilder aggregate) {
            final TemplateTypeImpl<Pass> type;

            switch (literalExpr.literal.getKind()) {
                case STRING_LITERAL:
                    type = ((AbstractTemplateTypeConstructor<Pass>) pass.getTypeSystem().lookUp("String")).applyTrivially();
                    break;
                case NUMERICAL_LITERAL:
                    // TODO correct type based on literal
                    type = ((AbstractTemplateTypeConstructor<Pass>) pass.getTypeSystem().lookUp("Natural")).applyTrivially();
                    break;
                case NIL:
                    type = ((AbstractTemplateTypeConstructor<Pass>) pass.getTypeSystem().lookUp("Nil")).applyTrivially();
                    break;
                default:
                    throw null;
            }

            aggregate.defineTypeOf(literalExpr, type);
            inferencer.pushValue(type);

            return aggregate;
        }

        @Override
        public ImplementationDataBuilder visitLocalVariableDeclaration(LocalVariableDeclaration<Pass> localVariableDeclaration, ImplementationDataBuilder aggregate) {
            visitAll(localVariableDeclaration.annotations, aggregate);
            visit(localVariableDeclaration.type, aggregate);

            final ExpressionNode<Pass> value = localVariableDeclaration.value;
            if (value != null) {
                visit(value, aggregate);
                inferencer.infer(pass, getContextualConstraints(), aggregate.lookUpProperTypeOf(localVariableDeclaration.type));
            }

            return aggregate;
        }

        @Override
        public ImplementationDataBuilder visitMemberAccessExpression(final MemberAccessExpression<Pass> memberAccessExpression, ImplementationDataBuilder aggregate) {
            visit(memberAccessExpression.primary, aggregate);
            visit(memberAccessExpression.name, aggregate);

            final Optional<AbstractTypeList<Pass, ?>> explicitTypeArguments = Optional.from(memberAccessExpression.name.typeArgs)
                    .map(args -> args.stream()
                            .map(e -> pass.lookUpTypeOf(e))
                            .collect(AbstractTypeList.typeList()));

            inferencer.accessMember(new InferMemberAccess() {
                @Override
                public Optional<AbstractTypeList<Pass, ?>> getTypeArgs() {
                    return explicitTypeArguments;
                }

                @Override
                public void setTypeArgs(AbstractMember<Pass> member, AbstractTypeList<Pass, ?> typeArgs) {
                    typeArgs = explicitTypeArguments.getOrReturn(typeArgs);

                    aggregate
                            .defineTypeOf(memberAccessExpression, member.getType().apply(typeArgs))
                            .defineTypeArgumentsTo(memberAccessExpression.name, typeArgs);
                }

                @Override
                public String getName() {
                    return memberAccessExpression.name.ids.stream().map(Token::getLexeme).collect(joining());
                }
            });

            return aggregate;
        }

        @Override
        public ImplementationDataBuilder visitNameExpression(NameExpression<Pass> nameExpression, ImplementationDataBuilder aggregate) {
            visit(nameExpression.name, aggregate);

            final AbstractResolution resolution = getNs().resolve(Iterables.getOnlyElement(nameExpression.name.ids).getLexeme());
            final TypeMixin<Pass, ?> resolvedType = resolution.getType();

            if (resolution.isMember()) {
                final TypeConstructorMixin<Pass, ?, ?> typeConstructor = (TypeConstructorMixin<Pass, ?, ?>) resolvedType;

                visit(resolution.getImplicitPrimary(), aggregate);

                inferencer.accessMember(new InferMemberAccess() {
                    @Override
                    public void setTypeArgs(AbstractMember<Pass> member, AbstractTypeList<Pass, ?> typeArgs) {
                        typeArgs = getTypeArgs().getOrReturn(typeArgs);
                        final ProperTypeMixin<Pass, ?> properType = (ProperTypeMixin<Pass, ?>) typeConstructor.apply(typeArgs);

                        aggregate
                                .defineTypeArgumentsTo(nameExpression.name, typeArgs)
                                .defineTypeOf(nameExpression, properType);
                    }

                    @Override
                    public Optional<AbstractTypeList<Pass, ?>> getTypeArgs() {
                        return Optional.from(nameExpression.name.typeArgs)
                                .map(args -> args.stream()
                                        .map(e -> aggregate.lookUpProperTypeOf(e))
                                        .collect(AbstractTypeList.typeList()));
                    }

                    @Override
                    public String getName() {
                        Preconditions.checkState(nameExpression.name.ids.size() == 1);
                        return nameExpression.name.ids.get(0).getLexeme();
                    }
                });
            } else {
                final ProperTypeMixin<Pass, ?> properType = (ProperTypeMixin<Pass, ?>) resolvedType;

                inferencer.pushValue(properType);
                aggregate.defineTypeOf(nameExpression, properType);
            }

            return aggregate.defineResolutionOf(nameExpression, resolution);
        }

        @Override
        public ImplementationDataBuilder visitPropertyDeclaration(PropertyDeclaration<Pass> propertyDeclaration, ImplementationDataBuilder aggregate) {
            visit(propertyDeclaration.initialValue, aggregate);
            inferencer.infer(pass, getContextualConstraints(), pass.lookUpProperTypeOf(propertyDeclaration.type));

            return aggregate;
        }

        @Override
        public ImplementationDataBuilder visitRootExpression(RootExpression<Pass> rootExpression, ImplementationDataBuilder aggregate) {
            final TemplateTypeImpl<Pass> type = pass.getGlueTypes().get(Fqn.from()).applyTrivially();

            inferencer.pushValue(type);

            return aggregate.defineTypeOf(rootExpression, type);
        }

        @Override
        public ImplementationDataBuilder visitThisExpr(ThisExpr<Pass> thisExpr, ImplementationDataBuilder aggregate) {
            ProperTypeMixin<Pass, ?> type = pass.lookUpTypeOf(getContainingTypeDeclaration()).applyTrivially();

            aggregate.defineTypeOf(thisExpr, type);
            inferencer.pushValue(type);

            return aggregate;
        }

        private class FunctionInvocationInference implements InferFunctionInvocation {
            private final FunctionInvocation<Pass> functionInvocation;
            private final Consumer<FunctionInvocation.Signature<Pass>> handler;

            public FunctionInvocationInference(FunctionInvocation<Pass> functionInvocation, Consumer<FunctionInvocation.Signature<Pass>> handler) {
                this.functionInvocation = functionInvocation;
                this.handler = handler;
            }

            public FunctionInvocationInference(FunctionInvocation<Pass> functionInvocation) {
                this(functionInvocation, s -> {});
            }

            @Override
            public void setSignature(int index, ProperTypeMixin<Pass, ?> explicit, ProperTypeMixin<Pass, ?> implicit) {
                final FunctionInvocation.Signature<Pass> signature = new FunctionInvocation.Signature<Pass>() {
                    @Override
                    public int getSignatureIndex() {
                        return index;
                    }

                    @Override
                    public ProperTypeMixin<Pass, ?> getExplicitSignatureType() {
                        return explicit;
                    }

                    @Override
                    public ProperTypeMixin<Pass, ?> getImplicitSignatureType() {
                        return implicit;
                    }
                };

                handler.accept(signature);
            }

            @Override
            public int getArgCount() {
                return functionInvocation.getArgs().size();
            }

            @Override
            public String toString() {
                try {
                    return functionInvocation.getSourceSnippet().getLexeme();
                } catch (Exception e) {
                    return functionInvocation.toString();
                }
            }
        }
    }
}
