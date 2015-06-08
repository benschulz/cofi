package de.benshu.cofi.cofic.frontend.infer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.Implicits;
import de.benshu.cofi.cofic.frontend.Owners;
import de.benshu.cofi.inference.Parametrization;
import de.benshu.cofi.types.Variance;
import de.benshu.cofi.types.impl.AdHoc;
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
import de.benshu.cofi.types.impl.functions.FunctionType;
import de.benshu.cofi.types.impl.intersections.AbstractIntersectionType;
import de.benshu.cofi.types.impl.intersections.AnonymousIntersectionType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Optional;
import de.benshu.commons.core.Pair;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static de.benshu.cofi.types.impl.constraints.AbstractConstraints.trivial;
import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;

public class OverloadedMemberAccessInferencer<T> implements OverloadedExpressionInferencer<T> {
    private static <T> int determineTypeArgCount(Pass pass, ExpressionInferencer<T> primary, InferMemberAccess<T> memberAccess,
                                                 AbstractMember<Pass> member) {
        return memberAccess.getTypeArgs()
                .map(args -> primary.getTypeArgCount())
                .getOrSupply(() -> primary.getTypeArgCount() + member.getType().getParameters().size());
    }

    private static <T> ProperTypeConstructorMixin<Pass, ?, ?> determineImplicitType(Pass pass, InferMemberAccess<T> memberAccess, AbstractMember<Pass> member) {
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
            public <O> O supplyConstraints(Pass context, TypeParameterListImpl<Pass> bound, Interpreter<AbstractConstraints<Pass>, O> interpreter) {
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

        final FunctionType<Pass> functionType = FunctionType.forceFrom(pass, type);
        AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>> paramTypes = functionType.getParameterTypes();
        ProperTypeMixin<Pass, ?> returnType = functionType.getReturnType();

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
    private final OverloadedExpressionInferencer<T> primary;
    private final InferMemberAccess<T> memberAccess;

    public OverloadedMemberAccessInferencer(Pass pass, OverloadedExpressionInferencer<T> primary, InferMemberAccess<T> memberAccess) {
        this.pass = pass;
        this.primary = primary;
        this.memberAccess = memberAccess;
    }

    @Override
    public Iterable<ExpressionInferencer<T>> unoverload() {
        return Iterables.concat(Iterables.transform(primary.unoverload(), new PerPrimaryMapper<T>(pass, primary, memberAccess)));
    }

    @Override
    public String toString() {
        return primary + "." + memberAccess.getName();
    }

    private static final class SimpleUnoverloaded<T> extends AbstractExpressionInferencer<T> {
        private final ExpressionInferencer<T> primary;
        private final ProperTypeMixin<Pass, ?> primaryContext;
        private final InferMemberAccess<T> memberAccess;
        private final AbstractMember<Pass> member;

        private SimpleUnoverloaded(Pass pass, ExpressionInferencer<T> primary, ProperTypeMixin<Pass, ?> primaryContext, InferMemberAccess<T> memberAccess, AbstractMember<Pass> member) {
            super(determineTypeArgCount(pass, primary, memberAccess, member));
            this.primary = primary;
            this.primaryContext = primaryContext;
            this.memberAccess = memberAccess;
            this.member = member;
        }

        @Override
        public Optional<Parametrization<Pass, T>> inferGeneric(Pass pass, TypeParameterListImpl<Pass> params, int offset,
                                                               AbstractConstraints<Pass> constraints, ProperTypeMixin<Pass, ?> context) {
            for (Parametrization<Pass, T> p : primary.inferGeneric(pass, params, offset, constraints, pass.getTypeSystem().getTop()))
                for (AbstractMember<Pass> m : p.getImplicitType().lookupMember(member.getName()))
                    return tryWrapParameterization(pass, p, m, offset, context);

            return none();
        }

        private Optional<Parametrization<Pass, T>> tryWrapParameterization(Pass pass, final Parametrization<Pass, T> p, final AbstractMember<Pass> m, int offset, ProperTypeMixin<Pass, ?> context) {
            int implicitTpCount = m.getTags().getOrFallbackToDefault(Implicits.TAG).getTypeParamCount();

            final int fromIndex = offset + primary.getTypeArgCount() + implicitTpCount;
            final int toIndex = offset + getTypeArgCount();

            ProperTypeConstructorMixin<Pass, ?, ?> implicitDeclaredType = determineImplicitType(pass, memberAccess, m);

            TypeParameterListImpl<Pass> params = p.getConstraints().getTypeParams();
            Implicits implicits = m.getTags().getOrFallbackToDefault(Implicits.TAG);
            ProperTypeMixin<Pass, ?> implicitType = dropImplicitParams(pass, implicitDeclaredType, implicits).apply(params.getVariables().subList(fromIndex, toIndex));

            final AbstractConstraints<Pass> constraints = transferConstraints(pass, m, p.getConstraints(), fromIndex, toIndex)
                    .establishSubtype(implicitType, context);

            if (constraints.isAll())
                return none();

            return some(new Parametrization<Pass, T>() {
                @Override
                public ProperTypeMixin<Pass, ?> getImplicitType() {
                    return implicitType;
                }

                @Override
                public ProperTypeMixin<Pass, ?> getExplicitType() {
                    TypeParameterListImpl<Pass> params = p.getConstraints().getTypeParams();
                    return implicitDeclaredType.apply(params.getVariables().subList(fromIndex, toIndex));
                }

                @Override
                public AbstractConstraints<Pass> getConstraints() {
                    return constraints;
                }

                @Override
                public T apply(Substitutions<Pass> substitutions, T aggregate) {
                    aggregate = p.apply(substitutions, aggregate);

                    int implicitTpCount = m.getTags().getOrFallbackToDefault(Implicits.TAG).getTypeParamCount();
                    if (implicitTpCount > 0) {
                        if (toIndex != fromIndex)
                            throw null;

                        aggregate = memberAccess.setTypeArgs(m, inferImplicitTypeArgs(pass, m), aggregate);
                    } else {
                        aggregate = memberAccess.setTypeArgs(m, getConstraints().getTypeParams().getVariables().subList(fromIndex, toIndex).map(substitutions::substitute), aggregate);
                    }

                    return aggregate;
                }
            });
        }

        @Override
        Optional<ProperTypeMixin<Pass, ?>> doInferSpecific(Pass pass) {
            // TODO the specific type args of m must be inferred beforehand and
            //      one instance created for each "specific" parameterization
            return some(member.getType().applyTrivially());
        }

        @Override
        public String toString() {
            return primary + "." + memberAccess.getName();
        }
    }

    private static final class PerPrimaryMapper<T> implements Function<ExpressionInferencer<T>, Iterable<ExpressionInferencer<T>>> {
        private final Pass pass;
        private final InferMemberAccess<T> memberAccess;

        public PerPrimaryMapper(Pass pass, OverloadedExpressionInferencer<T> primary, InferMemberAccess<T> memberAccess) {
            this.pass = pass;
            this.memberAccess = memberAccess;
        }

        @Override
        public Iterable<ExpressionInferencer<T>> apply(ExpressionInferencer<T> primary) {
            String memberName = memberAccess.getName();

            for (ProperTypeMixin<Pass, ?> pst : primary.inferSpecific(pass))
                for (AbstractMember<Pass> member : pst.lookupMember(memberName)) {
                    return member.getTags().get(Owners.TAG).getOwners()
                            .mapAny(o -> new SimpleUnoverloaded<T>(pass, primary, o, memberAccess, o.lookupMember(memberName).get()));
                }

            return ImmutableList.of();
        }
    }
}
