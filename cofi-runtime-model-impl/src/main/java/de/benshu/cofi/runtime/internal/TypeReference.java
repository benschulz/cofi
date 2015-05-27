package de.benshu.cofi.runtime.internal;

import de.benshu.cofi.types.Type;

public interface TypeReference<T extends Type> {
    T resolve(TypeReferenceContext context);
}
