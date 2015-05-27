package de.benshu.cofi.types.impl.constraints;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import de.benshu.cofi.types.bound.Constraints;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeVariableImpl;

import static com.google.common.base.Preconditions.checkArgument;

final class NoneImpl<X extends TypeSystemContext<X>> extends Monosemous<X> implements Constraints.None<X> {
    @Override
    public Monosemous<X> getParent() {
        return this;
    }

    @Override
    public TypeParameterListImpl<X> getTypeParams() {
        return TypeParameterListImpl.empty();
    }

    @Override
    public AbstractConstraints<X> substitute(X context, TypeParameterListImpl<X> parameters, Substitutions<X> substitutions) {
        return Conjunction.trivial(context, parameters.getConstraints(), TypeParameterListImpl.<X>empty());
    }

    @Override
    public AbstractConstraints<X> and(AbstractConstraints<X> constraints) {
        return constraints;
    }

    @Override
    public boolean isAll() {
        return false;
    }

    @Override
    public boolean isDisjunctive() {
        return false;
    }

    @Override
    public boolean isNone() {
        return true;
    }

    @Override
    public AbstractConstraints<X> or(AbstractConstraints<X> constraints) {
        return this;
    }

    @Override
    public AbstractConstraints<X> simplify(X context) {
        return this;
    }

    @Override
    public String toString() {
        return "\u22A5 <: \u22A4";
    }

    @Override
    public AbstractConstraints<X> establishSubtype(TypeMixin<X, ?> subtype, TypeMixin<X, ?> supertype) {
        return subtype.establishSubtype(supertype, this);
    }

    @Override
    public boolean isSubtype(TypeMixin<X, ?> subtype, TypeMixin<X, ?> supertype) {
        return establishSubtype(subtype, supertype).isNone();
    }

    @Override
    public boolean contains(AbstractConstraints<X> cs) {
        return cs.isNone();
    }

    @Override
    public boolean checkBounds(X context, AbstractConstraints<X> contextualConstraints, Substitutions<X> substitutions) {
        checkArgument(substitutions.isEmpty());

        return true;
    }

    @Override
    public ImmutableList<ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>>> getConstraints() {
        return ImmutableList.of();
    }

    @Override
    public ImmutableSet<Constraint<X>> getConstraints(TypeVariableImpl<X, ?> variable) {
        throw new IllegalArgumentException();
    }

    @Override
    public AbstractConstraints<X> reconcile(X context) {
        return this;
    }

    @Override
    public TypeMixin<X, ?> getUpperBound(X context, TypeVariableImpl<X, ?> typeVariable) {
        throw new IllegalArgumentException();
    }

    @Override
    public TypeMixin<X, ?> getLowerBound(X context, TypeVariableImpl<X, ?> typeVariable) {
        throw new IllegalArgumentException();
    }
}