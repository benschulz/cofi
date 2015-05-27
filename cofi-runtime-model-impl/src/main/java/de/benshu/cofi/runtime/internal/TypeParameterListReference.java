package de.benshu.cofi.runtime.internal;

import de.benshu.cofi.types.TypeParameterList;

public interface TypeParameterListReference {
    TypeParameterList resolve(TypeReferenceContext context);
}
