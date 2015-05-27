package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.Kind;
import de.benshu.cofi.types.ProperTypeConstructor;

public interface ProperTypeConstructorMixin<X extends TypeSystemContext<X>, S extends TypeConstructorMixin<X, S, T>, T extends ProperTypeMixin<X, ?>>
        extends TypeConstructorMixin<X, S, T> {

    @Override
    default Kind getKind() {
        return KindImpl.FIRST_ORDER;
    }

    @Override
    ProperTypeConstructor<?> unbind();
}
