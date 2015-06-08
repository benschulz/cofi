package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.ProperTypeConstructor;

public abstract class AbstractUnboundProperTypeConstructor<X extends TypeSystemContext<X>, U extends ProperTypeConstructorMixin<X, ?, ? extends ProperTypeMixin<X, ?>>, T extends ProperType>
        extends AbstractUnboundTypeConstructor<X, U, T>
        implements ProperTypeConstructor<T> {

    public AbstractUnboundProperTypeConstructor(U unbound) {
        super(unbound);
    }
}
