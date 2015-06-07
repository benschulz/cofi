package de.benshu.cofi.cofic.model.binary.internal;

import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;

public interface UnboundTypeParameterList {
    <X extends TypeSystemContext<X>> TypeParameterListImpl<X> bind(X context);
}
