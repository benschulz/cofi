package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.ConstructedType;
import de.benshu.cofi.types.TypeConstructor;
import de.benshu.cofi.types.TypeList;

public abstract class AbstractUnboundConstructedType<X extends TypeSystemContext<X>, U extends AbstractProperType<X, U> & ConstructedTypeMixin<X, U, C>, C extends TypeConstructorMixin<X, C, U>, BC extends TypeConstructor<?>>
        extends AbstractUnboundProperType<X, U>
        implements ConstructedType<BC> {

    public AbstractUnboundConstructedType(U unbound) {
        super(unbound);
    }

    public TypeList<?> getArguments() {
        return bound.getArguments().unbind();
    }
}
