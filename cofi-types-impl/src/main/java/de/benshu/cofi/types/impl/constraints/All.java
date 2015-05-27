package de.benshu.cofi.types.impl.constraints;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import de.benshu.cofi.types.impl.*;

final class All<X extends TypeSystemContext<X>> extends AbstractConstraints<X> {
    @Override
    public AbstractConstraints<X> and(AbstractConstraints<X> constraints) {
        return this;
    }

    @Override
    public boolean isAll() {
        return true;
    }

    @Override
    public boolean isDisjunctive() {
        return false;
    }

    @Override
    public boolean isNone() {
        return false;
    }

    @Override
    public AbstractConstraints<X> getParent() {
        return this;
    }

    @Override
    public AbstractConstraints<X> or(AbstractConstraints<X> constraints) {
        return constraints;
    }

    @Override
    public AbstractConstraints<X> simplify(X context) {
        return this;
    }

    @Override
    public String toString() {
        return "\u22A4 <: \u22A5";
    }

    @Override
    public AbstractConstraints<X> establishSubtype(TypeMixin<X, ?> subtype, TypeMixin<X, ?> supertype) {
        return this;
    }

    @Override
    public boolean isSubtype(TypeMixin<X, ?> subtype, TypeMixin<X, ?> supertype) {
        return true;
    }

    @Override
    public boolean contains(AbstractConstraints<X> cs) {
        return false;
    }

    @Override
    public boolean checkBounds(X context, AbstractConstraints<X> contextualConstraints, Substitutions<X> substitutions) {
        return false;
    }

    @Override
    public ImmutableList<ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>>> getConstraints() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractConstraints<X> reconcile(X context) {
        return this;
    }

    @Override
    public TypeParameterListImpl<X> getTypeParams() {
        throw new AssertionError();
    }

    @Override
    public AbstractConstraints<X> substitute(X context, TypeParameterListImpl<X> parameters, Substitutions<X> substitutions) {
        return this;
    }
}