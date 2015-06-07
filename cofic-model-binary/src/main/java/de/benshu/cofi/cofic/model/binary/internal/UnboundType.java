package de.benshu.cofi.cofic.model.binary.internal;

import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;

public interface UnboundType {
    <X extends TypeSystemContext<X>> TypeMixin<X, ?> bind(X context);
}
