package de.benshu.cofi.cofic.model.binary.internal;

import de.benshu.cofi.binary.deserialization.internal.TypeReferenceContext;
import de.benshu.cofi.binary.deserialization.internal.UnboundTypeParameterList;

public interface TypeParameterListReference {
    UnboundTypeParameterList resolve(TypeReferenceContext context);
}
