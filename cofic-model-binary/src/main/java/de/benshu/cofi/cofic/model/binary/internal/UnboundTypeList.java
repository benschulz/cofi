package de.benshu.cofi.cofic.model.binary.internal;

import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

public interface UnboundTypeList {
    <X extends TypeSystemContext<X>> AbstractTypeList<X, ?> bind(X context);
}
