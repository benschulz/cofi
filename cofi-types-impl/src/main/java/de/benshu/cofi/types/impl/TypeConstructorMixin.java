package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.bound.TypeConstructor;
import de.benshu.cofi.types.bound.TypeList;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

public interface TypeConstructorMixin<X extends TypeSystemContext<X>, S extends TypeConstructorMixin<X, S, T>, T extends TypeMixin<X, ?>>
        extends TypeMixin<X, S>, TypeConstructor<X, S, T> {

    @Override
    TypeConstructorMixin<X, ?, ?> substitute(Substitutions<X> substitutions);

    @Override
    TypeParameterListImpl<X> getParameters();

    @Override
    default T apply(TypeList<X, ?> arguments) {
        return apply((AbstractTypeList<X, ?>) arguments);
    }

    default T applyTrivially() {
        return apply(getParameters().getVariables());
    }

    T apply(AbstractTypeList<X, ?> arguments);

    @Override
    de.benshu.cofi.types.TypeConstructor<?> unbind();

    @Override
    default String debug() {
        return getParameters().getVariables().debug() + "\u21A6" + applyTrivially().debug();
    }
}
