package de.benshu.cofi.types.impl;

public interface UnboundProperTypeConstructor<X extends TypeSystemContext<X>> {
    ProperTypeConstructorMixin<X, ?, ?> bind(X context);
}
