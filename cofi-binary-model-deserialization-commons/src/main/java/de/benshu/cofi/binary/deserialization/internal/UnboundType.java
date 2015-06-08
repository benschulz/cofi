package de.benshu.cofi.binary.deserialization.internal;

import de.benshu.cofi.types.impl.TypeMixin;

public interface UnboundType {
    <X extends BinaryModelContext<X>> TypeMixin<X, ?> bind(X context);
}
