package de.benshu.cofi.runtime.internal;

import de.benshu.cofi.types.Constraints;
import de.benshu.commons.core.Optional;

public interface TypeReferenceContext {
    Optional<Constraints> getOuterConstraints();

    Constraints getConstraints();
}
