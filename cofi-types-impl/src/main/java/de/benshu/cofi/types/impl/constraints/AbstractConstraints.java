package de.benshu.cofi.types.impl.constraints;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import de.benshu.cofi.types.bound.Constraints;
import de.benshu.cofi.types.bound.Type;
import de.benshu.cofi.types.bound.TypeList;
import de.benshu.cofi.types.impl.AbstractType;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.SomeTypeSystemContext;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterImpl;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.TypeVariableImpl;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractConstraints<X extends TypeSystemContext<X>> implements Constraints<X> {
    private static final All<?> ALL = new All<SomeTypeSystemContext>();
    private static final NoneImpl<?> NONE = new NoneImpl<SomeTypeSystemContext>();

    public static <X extends TypeSystemContext<X>> AbstractConstraints<X> all() {
        return (AbstractConstraints<X>) ALL;
    }

    public static <X extends TypeSystemContext<X>> Monosemous<X> none() {
        return (Monosemous<X>) NONE;
    }

    public static <X extends TypeSystemContext<X>> Monosemous<X> trivial(X context, TypeParameterListImpl<X> params) {
        return (Monosemous<X>) trivial(context, none(), params);
    }

    public static <X extends TypeSystemContext<X>> AbstractConstraints<X> trivial(X context, AbstractConstraints<X> parent, TypeParameterListImpl<X> params) {
        if (params.isEmpty() && parent.isNone()) {
            return parent;
        }

        final TypeSystemImpl<X> types = context.getTypeSystem();
        final ProperTypeMixin<X, ?> top = types.getTop();
        final ProperTypeMixin<X, ?> bot = types.getBottom();

        final ImmutableSetMultimap.Builder<TypeVariableImpl<X, ?>, Constraint<X>> constraints = ImmutableSetMultimap.builder();

        for (TypeParameterImpl<X> p : params.iterable()) {
            constraints.put(p.getVariable(), Constraint.upper(top));
            constraints.put(p.getVariable(), Constraint.lower(bot));
        }

        // TODO first argument unhappy-paths
        return params.isEmpty() && parent.getTypeParams().isEmpty() ? parent
                : new Conjunction<>((Monosemous<X>) parent, params, constraints.build());
    }

    AbstractConstraints() {
    }

    public abstract boolean isAll();

    public abstract boolean isDisjunctive();

    public abstract boolean isNone();

    public abstract AbstractConstraints<X> getParent();

    public abstract AbstractConstraints<X> and(AbstractConstraints<X> cs);

    public abstract AbstractConstraints<X> or(AbstractConstraints<X> cs);

    public abstract AbstractConstraints<X> reconcile(X context);

    public abstract AbstractConstraints<X> simplify(X context);

    @Override
    public final AbstractConstraints<X> establishSubtype(Type<X, ?> subtype, Type<X, ?> supertype) {
        Preconditions.checkArgument(subtype instanceof AbstractType<?, ?>);
        Preconditions.checkArgument(supertype instanceof AbstractType<?, ?>);

        return establishSubtype((TypeMixin<X, ?>) subtype, (TypeMixin<X, ?>) supertype);
    }

    public abstract AbstractConstraints<X> establishSubtype(TypeMixin<X, ?> subtype, TypeMixin<X, ?> supertype);

    @Override
    public boolean isSubtype(Type<X, ?> subtype, Type<X, ?> supertype) {
        return isSubtype((TypeMixin<X, ?>) subtype, (TypeMixin<X, ?>) supertype);
    }

    public abstract boolean isSubtype(TypeMixin<X, ?> subtype, TypeMixin<X, ?> supertype);

    @Override
    public boolean areSubtypes(TypeList<X, ?> subtypes, TypeList<X, ?> supertypes) {
        return areSubtypes((AbstractTypeList<X, ?>) subtypes, (AbstractTypeList<X, ?>) supertypes);
    }

    /**
     * @param subtypes
     * @param supertypes
     * @return {@code true} iff {@code subtypes[i]} <: {@code supertypes[i]}, for all valid {@code i}s
     */
    public final boolean areSubtypes(AbstractTypeList<X, ?> subtypes, AbstractTypeList<X, ?> supertypes) {
        for (int i = 0; i < subtypes.size(); ++i) {
            if (!isSubtype(subtypes.get(i), supertypes.get(i))) {
                return false;
            }
        }

        return true;
    }

    public boolean areEqualTypes(Type<X, ?> a, Type<X, ?> b) {
        return isSubtype(a, b) && isSubtype(b, a);
    }

    public boolean areEqualTypes(AbstractTypeList<X, ?> as, AbstractTypeList<X, ?> bs) {
        return areSubtypes(as, bs) && areSubtypes(bs, as);
    }

    public abstract ImmutableList<ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>>> getConstraints();

    /**
     * Checks whether {@code substitutions} satisfy the bounds on the parameters of this constraint set.
     * <p/>
     * <p>
     * Substitutes {@code substitutions} for the parameters of this constraint set. The resulting bounds are
     * then checked against the arguments using the {@code contextualConstraints}.
     * </p>
     *
     * @param context
     * @param contextualConstraints context of the substitution
     * @param substitutions         substitutions for the parameters of this constraint set
     * @return
     */
    public abstract boolean checkBounds(X context, AbstractConstraints<X> contextualConstraints, Substitutions<X> substitutions);

    public abstract boolean contains(AbstractConstraints<X> cs);

    public AbstractConstraints<X> append(AbstractConstraints<X> cs) {
        // FIXME only happy paths for now, also use dynamic dispatch
        if (isNone()) {
            return cs;
        } else if (isAll()) {
            return this;
        } else if (isDisjunctive()) {
            throw null;
        } else {
            Conjunction<X> parent = (Conjunction<X>) this;

            if (cs.isNone()) {
                return parent.getTypeParams().isEmpty() ? parent
                        : new Conjunction<>(
                        parent,
                        TypeParameterListImpl.empty(),
                        ImmutableSetMultimap.of(),
                        ImmutableMap.of());
            } else {
                Conjunction<X> csC = (Conjunction<X>) cs;
                checkState(csC.getParent().isNone());

                return new Conjunction<>(parent, csC.getTypeParams(), csC.getMonoConstraints(), csC.getEquivalents());
            }
        }
    }

    public AbstractConstraints<X> transferTo(X context, AbstractConstraints<X> cs, int fromIndex) {
        throw null;
    }

    public AbstractConstraints<X> transferTo(X context, AbstractConstraints<X> target, Substitutions<X> substitutions) {
        AbstractTypeList<X, ?> arguments = target.getTypeParams().getVariables().map(substitutions::substitute);
        return transferTo(context, target, arguments);
    }

    public AbstractConstraints<X> transferTo(X context, AbstractConstraints<X> target, AbstractTypeList<X, ?> arguments) {
        throw null;
    }

    public abstract TypeParameterListImpl<X> getTypeParams();

    public de.benshu.cofi.types.Constraints unbind() {
        return new Unbound<>(this);
    }

    public static <X extends TypeSystemContext<X>> AbstractConstraints<X> rebind(de.benshu.cofi.types.Constraints unbound) {
        return ((Unbound<X>) unbound).bound;
    }

    public abstract AbstractConstraints<X> substitute(X context, TypeParameterListImpl<X> parameters, Substitutions<X> substitutions);

    private static class Unbound<X extends TypeSystemContext<X>> implements de.benshu.cofi.types.Constraints {
        private final AbstractConstraints<X> bound;

        public Unbound(AbstractConstraints<X> bound) {
            this.bound = bound;
        }

        @Override
        public de.benshu.cofi.types.Constraints establishSubtype(de.benshu.cofi.types.Type subtype, de.benshu.cofi.types.Type supertype) {
            return bound.establishSubtype(TypeMixin.rebind(subtype), TypeMixin.rebind(supertype)).unbind();
        }

        @Override
        public boolean areSubtypes(de.benshu.cofi.types.TypeList<?> subtypes, de.benshu.cofi.types.TypeList<?> supertypes) {
            return bound.areSubtypes(AbstractTypeList.<X>rebind(subtypes), AbstractTypeList.<X>rebind(supertypes));
        }

        @Override
        public boolean isSubtype(de.benshu.cofi.types.Type subtype, de.benshu.cofi.types.Type supertype) {
            return bound.isSubtype(TypeMixin.rebind(subtype), TypeMixin.rebind(supertype));
        }

        @Override
        public boolean areEqualTypes(de.benshu.cofi.types.Type a, de.benshu.cofi.types.Type b) {
            return bound.areEqualTypes(TypeMixin.rebind(a), TypeMixin.rebind(b));
        }
    }
}
