package de.benshu.cofi.cofic.model.binary.internal;

import de.benshu.cofi.binary.deserialization.internal.TypeReferenceContext;

public interface TypeParameterListReference {
    UnboundTypeParameterList resolve(TypeReferenceContext context);
}
