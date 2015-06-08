package de.benshu.cofi.cofic.frontend.implementations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.MemoizingSupplier;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.infer.ExpressionTreeInferencer;
import de.benshu.cofi.cofic.frontend.infer.InferClosure;
import de.benshu.cofi.cofic.frontend.infer.InferFunctionInvocation;
import de.benshu.cofi.cofic.frontend.infer.InferMemberAccess;
import de.benshu.cofi.cofic.frontend.namespace.AbstractResolution;
import de.benshu.cofi.cofic.frontend.namespace.NamespaceTrackingVisitor;
import de.benshu.cofi.cofic.frontend.namespace.ParametersNs;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.Closure;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.model.impl.ExpressionStatement;
import de.benshu.cofi.model.impl.FunctionInvocationExpression;
import de.benshu.cofi.model.impl.LiteralExpression;
import de.benshu.cofi.model.impl.LocalVariableDeclaration;
import de.benshu.cofi.model.impl.MemberAccessExpression;
import de.benshu.cofi.model.impl.NameExpression;
import de.benshu.cofi.model.impl.PropertyDeclaration;
import de.benshu.cofi.model.impl.RootExpression;
import de.benshu.cofi.model.impl.Statement;
import de.benshu.cofi.model.impl.ThisExpression;
import de.benshu.cofi.model.impl.TransformationContext;
import de.benshu.cofi.model.impl.TransformedUserDefinedNode;
import de.benshu.cofi.parser.lexer.ArtificialToken;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.functions.FunctionType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.commons.core.Optional;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;
import static de.benshu.cofi.types.impl.lists.AbstractTypeList.typeList;
import static de.benshu.commons.core.streams.Collectors.list;
import static de.benshu.commons.core.streams.Collectors.single;
import static java.util.stream.Collectors.joining;

public class ImplementationTyper {
    public static ImplementationData type(Pass pass, ImmutableSet<CompilationUnit<Pass>> compilationUnits) {
        return compilationUnits
                .stream()
                .map(u -> new Visitor(pass).visit(u, ImplementationData.builder()))
                .collect(ImplementationData::builder, ImplementationDataBuilder::addAll, ImplementationDataBuilder::addAll)
                .addAll(new ImplementationDataBuilder(pass.getGenericModelData()))
                .build();
    }

    private static final class Visitor extends NamespaceTrackingVisitor<ImplementationDataBuilder> {
        private final Pass pass;
        private ExpressionTreeInferencer<ImplementationDataBuilder> inferencer;

        public Visitor(Pass pass) {
            super(pass);

            this.pass = pass;
            this.inferencer = new ExpressionTreeInferencer<>(pass);
        }

        @Override
        public ImplementationDataBuilder visitClosure(Closure<Pass> closure, ImplementationDataBuilder aggregate) {
            ExpressionTreeInferencer<ImplementationDataBuilder> backup = inferencer;
            inferencer = new ExpressionTreeInferencer<>(pass);
            for (Closure.Case<Pass> caze : closure.cases) {
                pushNs(ParametersNs.wrap(getNs(), caze.params));
                visitAll(caze.params, aggregate);
                aggregate = visitStatements(caze.body, aggregate);
                popNs();
            }
            inferencer = backup;

            ImplementationDataBuilder finalAggregate = aggregate;
            inferencer.pushClosure(new ClosureInference(closure, MemoizingSupplier.of(() -> closure.cases.get(0).params.stream()
                    .map(p -> finalAggregate.lookUpProperTypeOf(p.type))
                    .collect(typeList()))));

            return aggregate;
        }

        @Override
        public ImplementationDataBuilder visitExpressionStatement(ExpressionStatement<Pass> expressionStatement, ImplementationDataBuilder aggregate) {
            visitAll(expressionStatement.annotations, aggregate);
            aggregate = visit(expressionStatement.expression, aggregate);

            return inferencer.infer(pass, getContextualConstraints(aggregate), pass.getTypeSystem().getTop(), aggregate)
                    .orElse(null);
        }

        @Override
        public ImplementationDataBuilder visitFunctionInvocationExpression(FunctionInvocationExpression<Pass> functionInvocationExpression, ImplementationDataBuilder aggregate) {
            visit(functionInvocationExpression.primary, aggregate);

            inferencer.beginInvocation(new InferFunctionInvocation<ImplementationDataBuilder>() {
                @Override
                public ImplementationDataBuilder setSignature(int index, ProperTypeMixin<Pass, ?> explicit, ProperTypeMixin<Pass, ?> implicit, ImplementationDataBuilder aggregate) {
                    final ProperTypeMixin<Pass, ?> returnType = FunctionType.forceFrom(pass, explicit).getReturnType();
                    return aggregate.defineTypeOf(functionInvocationExpression, returnType);
                }

                @Override
                public int getArgCount() {
                    return functionInvocationExpression.getArgs().size();
                }

                @Override
                public String toString() {
                    try {
                        return functionInvocationExpression.getSourceSnippet().getLexeme();
                    } catch (Exception e) {
                        return functionInvocationExpression.toString();
                    }
                }
            });

            aggregate = visitAll(functionInvocationExpression.args, aggregate);
            inferencer.endInvocation();

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

                return inferencer.infer(pass, getContextualConstraints(aggregate), aggregate.lookUpProperTypeOf(localVariableDeclaration.type), aggregate)
                        .orElse(null);
            }

            return aggregate;
        }

        @Override
        public ImplementationDataBuilder visitMemberAccessExpression(final MemberAccessExpression<Pass> memberAccessExpression, ImplementationDataBuilder aggregate) {
            visit(memberAccessExpression.primary, aggregate);
            visit(memberAccessExpression.name, aggregate);

            final Optional<AbstractTypeList<Pass, ?>> explicitTypeArguments = Optional.from(memberAccessExpression.name.typeArgs)
                    .map(args -> args.stream()
                            .map(aggregate::lookUpTypeOf)
                            .collect(AbstractTypeList.typeList()));

            inferencer.accessMember(new MemberAccessInference(explicitTypeArguments, memberAccessExpression));

            return aggregate;
        }

        @Override
        public ImplementationDataBuilder visitNameExpression(NameExpression<Pass> nameExpression, ImplementationDataBuilder aggregate) {
            visit(nameExpression.name, aggregate);

            final AbstractResolution resolution = resolve(nameExpression.name, aggregate);
            final TypeMixin<Pass, ?> resolvedType = resolution.getType();

            if (resolution.isMember()) {
                final TypeConstructorMixin<Pass, ?, ?> typeConstructor = (TypeConstructorMixin<Pass, ?, ?>) resolvedType;

                visit(resolution.getImplicitPrimary(), aggregate);

                final Optional<AbstractTypeList<Pass, ?>> explicitTypeArguments = Optional.from(nameExpression.name.typeArgs)
                        .map(args -> args.stream()
                                .map(aggregate::lookUpTypeOf)
                                .collect(AbstractTypeList.typeList()));

                inferencer.accessMember(new NameExpressionInference(typeConstructor, nameExpression, explicitTypeArguments));
            } else {
                final ProperTypeMixin<Pass, ?> properType = (ProperTypeMixin<Pass, ?>) resolvedType;

                inferencer.pushValue(properType);
                aggregate.defineTypeOf(nameExpression, properType);
            }

            return aggregate.defineResolutionOf(nameExpression, resolution);
        }

        @Override
        public ImplementationDataBuilder visitPropertyDeclaration(PropertyDeclaration<Pass> propertyDeclaration, ImplementationDataBuilder aggregate) {
            ExpressionNode<Pass> initialValue = propertyDeclaration.initialValue;

            return new UserDefinedNodeTransformer<Pass>(transformationContext(aggregate)).transform(initialValue).stream()
                    .map(t -> {
                        ImplementationDataBuilder tmp = visit(t.getTransformedNode(), aggregate.copy().defineTransformation(initialValue, t.getTransformedNode()));
                        java.util.Optional<ImplementationDataBuilder> result = inferencer.infer(pass, getContextualConstraints(aggregate), pass.lookUpProperTypeOf(propertyDeclaration.type), tmp);

                        return java.util.Optional.ofNullable(result.isPresent() && t.test(transformationContext(result.get())) ? result.get() : null);
                    })
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(single());
        }

        @Override
        public ImplementationDataBuilder visitRootExpression(RootExpression<Pass> rootExpression, ImplementationDataBuilder aggregate) {
            final TemplateTypeImpl<Pass> type = pass.getGlueTypes().get(Fqn.root()).applyTrivially();

            inferencer.pushValue(type);

            return aggregate.defineTypeOf(rootExpression, type);
        }

        @Override
        protected ImplementationDataBuilder visitStatement(Statement<Pass> statement, ImplementationDataBuilder aggregate) {
            ImmutableList<TransformedUserDefinedNode<Pass, Statement<Pass>>> transformed = new UserDefinedNodeTransformer<Pass>(transformationContext(aggregate)).transform(statement).stream().collect(list());

            ImmutableList<ImplementationDataBuilder> validTransformations = transformed.stream()
                    .map(t -> {
                        ImplementationDataBuilder result = visit(t.getTransformedNode(), aggregate.copy().defineTransformation(statement, t.getTransformedNode()));

                        return java.util.Optional.ofNullable(result != null && t.test(transformationContext(result)) ? result : null);
                    })
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(list());

            return validTransformations.stream().collect(single());
        }

        @Override
        public ImplementationDataBuilder visitThisExpr(ThisExpression<Pass> thisExpression, ImplementationDataBuilder aggregate) {
            ProperTypeMixin<Pass, ?> type = pass.lookUpTypeOf(getContainingTypeDeclaration()).applyTrivially();

            aggregate.defineTypeOf(thisExpression, type);
            inferencer.pushValue(type);

            return aggregate;
        }

        private TransformationContext<Pass> transformationContext(ImplementationDataBuilder aggregate) {
            return new TransformationContext<Pass>() {
                @Override
                public TypeMixin<Pass, ?> resolveType(Fqn fqn) {
                    ArtificialToken hack = ArtificialToken.create(Token.Kind.IDENTIFIER, "hack");
                    return ImplementationTyper.Visitor.this.resolveFullyQualifiedType(fqn, hack.getTokenString(hack), aggregate);
                }

                @Override
                public TypeMixin<Pass, ?> resolve(String name) {
                    return ImplementationTyper.Visitor.this.resolve(name, aggregate).getType();
                }

                @Override
                public ProperTypeMixin<Pass, ?> lookUpTypeOf(ExpressionNode<Pass> expression) {
                    return aggregate.lookUpTypeOf(expression);
                }
            };
        }

        private static class ClosureInference implements InferClosure<ImplementationDataBuilder> {
            private final Closure<Pass> closure;
            private final Supplier<AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>>> parameterTypes;

            public ClosureInference(Closure<Pass> closure, Supplier<AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>>> parameterTypes) {
                this.closure = closure;
                this.parameterTypes = parameterTypes;
            }

            @Override
            public ImplementationDataBuilder setSignature(ProperTypeMixin<Pass, ?> signature, ImplementationDataBuilder aggregate) {
                return aggregate.defineTypeOf(closure, signature);
            }

            @Override
            public AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>> getParameterTypes() {
                return parameterTypes.get();
            }
        }

        private static class MemberAccessInference implements InferMemberAccess<ImplementationDataBuilder> {
            private final Optional<AbstractTypeList<Pass, ?>> explicitTypeArguments;
            private final MemberAccessExpression<Pass> memberAccessExpression;

            public MemberAccessInference(Optional<AbstractTypeList<Pass, ?>> explicitTypeArguments, MemberAccessExpression<Pass> memberAccessExpression) {
                this.explicitTypeArguments = explicitTypeArguments;
                this.memberAccessExpression = memberAccessExpression;
            }

            @Override
            public Optional<AbstractTypeList<Pass, ?>> getTypeArgs() {
                return explicitTypeArguments;
            }

            @Override
            public ImplementationDataBuilder setTypeArgs(AbstractMember<Pass> member, AbstractTypeList<Pass, ?> typeArgs, ImplementationDataBuilder aggregate) {
                typeArgs = explicitTypeArguments.getOrReturn(typeArgs);

                return aggregate
                        .defineTypeOf(memberAccessExpression, member.getType().apply(typeArgs))
                        .defineTypeArgumentsTo(memberAccessExpression.name, typeArgs);
            }

            @Override
            public String getName() {
                return memberAccessExpression.name.ids.stream().map(Token::getLexeme).collect(joining());
            }
        }

        private static class NameExpressionInference implements InferMemberAccess<ImplementationDataBuilder> {
            private final TypeConstructorMixin<Pass, ?, ?> typeConstructor;
            private final NameExpression<Pass> nameExpression;
            private final Optional<AbstractTypeList<Pass, ?>> explicitTypeArguments;

            public NameExpressionInference(TypeConstructorMixin<Pass, ?, ?> typeConstructor, NameExpression<Pass> nameExpression, Optional<AbstractTypeList<Pass, ?>> explicitTypeArguments) {
                this.typeConstructor = typeConstructor;
                this.nameExpression = nameExpression;
                this.explicitTypeArguments = explicitTypeArguments;
            }

            @Override
            public ImplementationDataBuilder setTypeArgs(AbstractMember<Pass> member, AbstractTypeList<Pass, ?> typeArgs, ImplementationDataBuilder aggregate) {
                typeArgs = getTypeArgs().getOrReturn(typeArgs);
                final ProperTypeMixin<Pass, ?> properType = (ProperTypeMixin<Pass, ?>) typeConstructor.apply(typeArgs);

                return aggregate
                        .defineTypeOf(nameExpression, properType)
                        .defineTypeArgumentsTo(nameExpression.name, typeArgs);
            }

            @Override
            public Optional<AbstractTypeList<Pass, ?>> getTypeArgs() {
                return explicitTypeArguments;
            }

            @Override
            public String getName() {
                checkState(nameExpression.name.ids.size() == 1);
                return nameExpression.name.ids.get(0).getLexeme();
            }
        }
    }
}
