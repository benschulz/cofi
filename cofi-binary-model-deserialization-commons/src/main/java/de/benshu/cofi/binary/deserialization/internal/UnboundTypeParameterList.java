package de.benshu.cofi.binary.deserialization.internal;

import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.types.impl.TypeParameterListImpl;

public interface UnboundTypeParameterList {
    <X extends BinaryModelContext<X>> TypeParameterListImpl<X> bind(X context);
}
