package de.benshu.cofi.cofic.model.binary.internal;

import de.benshu.cofi.binary.deserialization.internal.TypeReferenceContext;

public interface TypeReference {
    UnboundType resolve(TypeReferenceContext context);
}
