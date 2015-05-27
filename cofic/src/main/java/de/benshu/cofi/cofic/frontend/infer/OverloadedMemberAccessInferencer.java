package de.benshu.cofi.cofic.frontend.infer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.Implicits;
import de.benshu.cofi.cofic.frontend.Owners;
import de.benshu.cofi.inference.Parametrization;
import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.Variance;
import de.benshu.cofi.types.impl.intersections.AbstractIntersectionType;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.AdHoc;
import de.benshu.cofi.types.impl.intersections.AnonymousIntersectionType;
import de.benshu.cofi.types.impl.FunctionTypes;
import de.benshu.cofi.types.impl.NullaryTypeConstructor;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutable;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.declarations.Interpreter;
import de.benshu.cofi.types.impl.declarations.TypeParameterListDeclaration;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.members.MethodImpl;
import de.benshu.cofi.types.impl.members.MethodSignatureImpl;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Optional;
import de.benshu.commons.core.Pair;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static de.benshu.cofi.types.impl.constraints.AbstractConstraints.trivial;
import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;

public class OverloadedMemberAccessInferencer implements OverloadedExpressionInferencer {
    private static int determineTypeArgCount(Pass pass, ExpressionInferencer primary, InferMemberAccess memberAccess,
                                             Optional<? extends AbstractMember<Pass>> member) {
        for (AbstractMember<Pass> m : member) {
            return memberAccess.getTypeArgs()
                    .map(args -> primary.getTypeArgCount())
                    .getOrSupply(() -> primary.getTypeArgCount() + m.getType().getParameters().size());
        }

        return -1;
    }

    private static ProperTypeConstructorMixin<Pass, ?, ?> determineImplicitType(Pass pass, InferMemberAccess memberAccess, AbstractMember<Pass> member) {
        Implicits implicits = member.getTags().getOrFallbackToDefault(Implicits.TAG);
        ProperTypeConstructorMixin<Pass, ?, ?> type = member.getType();

        int implicitTpCount = implicits.getTypeParamCount();

        if (implicitTpCount > 0) {
            AbstractTypeList<Pass, ?> implicitTypeArgs = inferImplicitTypeArgs(pass, member);

            Substitutions<Pass> substitutions = Substitutions.firstOfThrough(type.getParameters(), implicitTypeArgs);

            type = applyPartially(type, pass, substitutions);
        }

        for (AbstractTypeList<Pass, ?> args : memberAccess.getTypeArgs())
            return NullaryTypeConstructor.create(type.apply(args));
        return type;
    }

    private static <T extends ProperTypeMixin<Pass, ?>> ProperTypeConstructorMixin<Pass, ?, ?> applyPartially(
            ProperTypeConstructorMixin<Pass, ?, T> typeConstructor,
            Pass pass,
            Substitutions<Pass> substitutions) {

        int newTpCount = typeConstructor.getParameters().size() - substitutions.size();
        // FIXME this unchecked cast does not seem safe for ad hoc constructors
        Substitutable<Pass, T> partiallySubstituted = Substitutable.unchecked(typeConstructor.applyTrivially().substitute(substitutions));

        final AtomicReference<TypeParameterListImpl<Pass>> hack = new AtomicReference<>();

        TypeParameterListImpl<Pass> newTps = TypeParameterListImpl.create(new TypeParameterListDeclaration<Pass>() {
            @Override
            public <O> O supplyParameters(Pass context, Interpreter<ImmutableList<Pair<Variance, IndividualTags>>, O> interpreter) {
                return interpreter.interpret(TypeParameterListImpl.describeN(newTpCount), context.getChecker());
            }

            @Override
            public <O> O supplyConstraints(Pass context, Interpreter<AbstractConstraints<Pass>, O> interpreter) {
                final AbstractConstraints<Pass> existing = typeConstructor.getParameters().getConstraints();
                final AbstractConstraints<Pass> partial = existing.transferTo(context, trivial(context, hack.get()), substitutions);
                return interpreter.interpret(partial, context.getChecker());
            }
        }).bind(pass);
        hack.set(newTps);

        return AdHoc.properTypeConstructor(pass, newTps, partiallySubstituted);
    }

    private static TypeConstructorMixin<Pass, ?, ? extends ProperTypeMixin<Pass, ?>> dropImplicitParams(Pass pass, TypeConstructorMixin<Pass, ?, ? extends ProperTypeMixin<Pass, ?>> type, Implicits implicits) {
        ProperTypeMixin<Pass, ?> properType = type.applyTrivially();

        if (properType instanceof AbstractIntersectionType<?, ?>) {
            AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>> elements = ((AbstractIntersectionType<Pass, ?>) properType).getElements();

            ImmutableList.Builder<TemplateTypeImpl<Pass>> builder = ImmutableList.builder();
            for (ProperTypeMixin<Pass, ?> element : elements) {
                builder.add(dropImplicitParamsRecursively(pass, element, implicits));
            }

            return AdHoc.typeConstructor(pass, type.getParameters(), AnonymousIntersectionType.createIfNonTrivial(pass, AbstractTypeList.of(builder.build())).substitutable());
        } else {
            return AdHoc.typeConstructor(pass, type.getParameters(),
                    dropImplicitParamsRecursively(pass, properType, implicits));
        }
    }

    private static TemplateTypeImpl<Pass> dropImplicitParamsRecursively(
            Pass pass, ProperTypeMixin<Pass, ?> type, Implicits implicits
    ) {
        if (implicits == Implicits.none()) {
            return (TemplateTypeImpl<Pass>) type;
        }

        int implicitParamCount = implicits.getParamCount();

        AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>> paramTypes = FunctionTypes.extractParamTypes(pass, type);
        ProperTypeMixin<Pass, ?> returnType = FunctionTypes.extractReturnType(pass, type);

        AbstractTemplateTypeConstructor<Pass> function = pass.getTypeSystem().getFunction(paramTypes.size() - implicitParamCount);

        return function.apply(AbstractTypeList.of(ImmutableList.copyOf(Iterables.concat(
                paramTypes.subList(implicitParamCount, paramTypes.size()),
                ImmutableList.of(dropImplicitParamsRecursively(pass, returnType, implicits.getTail()))))));
    }

    // TODO actually infer the type args
    private static AbstractTypeList<Pass, ?> inferImplicitTypeArgs(Pass pass, AbstractMember<Pass> member) {
        TypeConstructorMixin<Pass, ?, ? extends ProperTypeMixin<Pass, ?>> type = member.getType();
        int implicitTpCount = member.getTags().get(Implicits.TAG).getTypeParamCount();

        ProperTypeMixin<Pass, ?>[] implicitTypeArgs = new ProperTypeMixin[implicitTpCount];
        Arrays.fill(implicitTypeArgs, pass.getTypeSystem().getTop());

        return AbstractTypeList.of(implicitTypeArgs);
    }

    static AbstractConstraints<Pass> transferConstraints(Pass pass, AbstractMember<Pass> member, AbstractConstraints<Pass> cs, int fromIndex,
                                                         int toIndex) {
        if (toIndex == fromIndex) {
            return cs;
        }

        return member.getType().getParameters().getConstraints().transferTo(pass, cs, fromIndex);
    }

    private final Pass pass;
    private final OverloadedExpressionInferencer primary;
    private final InferMemberAccess memberAccess;

    public OverloadedMemberAccessInferencer(Pass pass, OverloadedExpressionInferencer primary, InferMemberAccess memberAccess) {
        this.pass = pass;
        this.primary = primary;
        this.memberAccess = memberAccess;
    }

    @Override
    public Iterable<ExpressionInferencer> unoverload() {
        return Iterables.concat(Iterables.transform(primary.unoverload(), new PerPrimaryMapper(pass, primary, memberAccess)));
    }

    @Override
    public String toString() {
        return primary + "." + memberAccess.getName();
    }

    private static final class SimpleUnoverloaded extends AbstractExpressionInferencer {
        private final ExpressionInferencer primary;
        private final ProperTypeMixin<Pass, ?> primaryContext;
        private final InferMemberAccess memberAccess;
        private final Optional<AbstractMember<Pass>> member;

        private SimpleUnoverloaded(Pass pass, ExpressionInferencer primary, ProperTypeMixin<Pass, ?> primaryContext, InferMemberAccess memberAccess, Optional<AbstractMember<Pass>> member) {
            super(determineTypeArgCount(pass, primary, memberAccess, member));
            this.primary = primary;
            this.primaryContext = primaryContext;
            this.memberAccess = memberAccess;
            this.member = member;
        }

        public SimpleUnoverloaded(Pass pass, ExpressionInferencer primary, ProperTypeMixin<Pass, ?> primaryContext, InferMemberAccess memberAccess) {
            this(pass, primary, primaryContext, memberAccess, primaryContext.lookupMember(memberAccess.getName()));
        }

        @Override
        public Optional<Parametrization<Pass>> inferGeneric(Pass pass, TypeParameterListImpl<Pass> params, int offset,
                                                            AbstractConstraints<Pass> constraints, ProperTypeMixin<Pass, ?> context) {
            for (AbstractMember<Pass> _ : member)
                for (Parametrization<Pass> p : primary.inferGeneric(pass, params, offset, constraints, primaryContext))
                    for (AbstractMember<Pass> m : p.getImplicitType().lookupMember(_.getName()))
                        return some(wrapParameterization(pass, p, m, offset));

            return none();
        }

        private Parametrization<Pass> wrapParameterization(Pass pass, final Parametrization<Pass> p, final AbstractMember<Pass> m, int offset) {
            int implicitTpCount = m.getTags().getOrFallbackToDefault(Implicits.TAG).getTypeParamCount();

            final int fromIndex = offset + primary.getTypeArgCount() + implicitTpCount;
            final int toIndex = offset + getTypeArgCount();

            final AbstractConstraints<Pass> constraints = transferConstraints(pass, m, p.getConstraints(), fromIndex, toIndex);

            return new Parametrization<Pass>() {
                @Override
                public ProperTypeMixin<Pass, ?> getImplicitType() {
                    TypeParameterListImpl<Pass> params = p.getConstraints().getTypeParams();
                    Implicits implicits = m.getTags().getOrFallbackToDefault(Implicits.TAG);
                    return dropImplicitParams(pass, getExplicitTypeConstructor(), implicits).apply(params.getVariables().subList(fromIndex, toIndex));
                }

                @Override
                public ProperTypeMixin<Pass, ?> getExplicitType() {
                    TypeParameterListImpl<Pass> params = p.getConstraints().getTypeParams();
                    return getExplicitTypeConstructor().apply(params.getVariables().subList(fromIndex, toIndex));
                }

                private TypeConstructorMixin<Pass, ?, ? extends ProperTypeMixin<Pass, ?>> getExplicitTypeConstructor() {
                    return determineImplicitType(pass, memberAccess, m);
                }

                @Override
                public AbstractConstraints<Pass> getConstraints() {
                    return constraints;
                }

                @Override
                public void apply(Substitutions<Pass> substitutions) {
                    p.apply(substitutions);

                    int implicitTpCount = m.getTags().getOrFallbackToDefault(Implicits.TAG).getTypeParamCount();
                    if (implicitTpCount > 0) {
                        if (toIndex != fromIndex)
                            throw null;

                        memberAccess.setTypeArgs(m, inferImplicitTypeArgs(pass, m));
                    } else {
                        memberAccess.setTypeArgs(m, getConstraints().getTypeParams().getVariables().subList(fromIndex, toIndex).map(substitutions::substitute));
                    }
                }
            };
        }

        @Override
        Optional<ProperTypeMixin<Pass, ?>> doInferSpecific(Pass pass) {
            for (AbstractMember<Pass> m : member) {
                // TODO the specific type args of m must be inferred beforehand and
                //      one instance created for each "specific" parameterization
                return Optional.<ProperTypeMixin<Pass, ?>>some(m.getType().applyTrivially());
            }

            return none();
        }

        @Override
        public String toString() {
            return primary + "." + memberAccess.getName();
        }
    }

    private static final class PerPrimaryMapper implements Function<ExpressionInferencer, Iterable<ExpressionInferencer>> {
        private final Pass pass;
        private final InferMemberAccess memberAccess;

        public PerPrimaryMapper(Pass pass, OverloadedExpressionInferencer primary, InferMemberAccess memberAccess) {
            this.pass = pass;
            this.memberAccess = memberAccess;
        }

        @Override
        public Iterable<ExpressionInferencer> apply(ExpressionInferencer primary) {
            for (ProperTypeMixin<Pass, ?> pst : primary.inferSpecific(pass)) {
                AbstractMember<Pass> member = pst.lookupMember(memberAccess.getName()).get();
                AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>> owners = member.getTags().get(Owners.TAG).getOwners();
                return owners.<ExpressionInferencer>mapAny(o -> new SimpleUnoverloaded(pass, primary, o, memberAccess));
            }
            return ImmutableList.of();
        }
    }
}
