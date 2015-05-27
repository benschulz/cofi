package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.Type;
import de.benshu.cofi.types.TypeConstructor;
import de.benshu.cofi.types.TypeParameterList;

public abstract class AbstractUnboundTypeConstructor<X extends TypeSystemContext<X>, U extends TypeConstructorMixin<X, ?, ?>, T extends Type>
        extends AbstractUnboundType<X, U>
        implements TypeConstructor<T> {

    public AbstractUnboundTypeConstructor(U unbound) {
        super(unbound);
    }

    @Override
    public TypeParameterList getParameters() {
        return bound.getParameters().unbind();
    }

    @Override
    public T applyTrivially() {
        return apply(bound.getParameters().getVariables().unbind());
    }
}
