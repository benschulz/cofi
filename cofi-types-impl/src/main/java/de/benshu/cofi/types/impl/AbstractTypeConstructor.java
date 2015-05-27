package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.Kind;

public abstract class AbstractTypeConstructor<X extends TypeSystemContext<X>, S extends TypeConstructorMixin<X, S, T>, T extends TypeMixin<X, ?>>
        extends AbstractType<X, S>
        implements TypeConstructorMixin<X, S, T> {

    protected AbstractTypeConstructor(X context) {
        super(context);
    }

    @Override
    public Kind getKind() {
        // FIXME this is wronk
        return KindImpl.FIRST_ORDER;
    }
}
