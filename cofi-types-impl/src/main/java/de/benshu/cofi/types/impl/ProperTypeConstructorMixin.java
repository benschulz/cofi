package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.Kind;
import de.benshu.cofi.types.ProperTypeConstructor;

public interface ProperTypeConstructorMixin<X extends TypeSystemContext<X>, S extends TypeConstructorMixin<X, S, T>, T extends ProperTypeMixin<X, ?>>
        extends TypeConstructorMixin<X, S, T> {

    static <X extends TypeSystemContext<X>> ProperTypeConstructorMixin<X, ?, ?> rebind(de.benshu.cofi.types.ProperTypeConstructor<?> unbound) {
        return (ProperTypeConstructorMixin<X, ?, ?>) TypeMixin.rebind(unbound);
    }

    @Override
    default Kind getKind() {
        return KindImpl.FIRST_ORDER;
    }

    @Override
    ProperTypeConstructor<?> unbind();
}
