package de.benshu.cofi.binary.deserialization.internal;

import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.commons.core.Optional;

public interface TypeReferenceContext {
    <X extends TypeSystemContext<X>> Optional<AbstractConstraints<X>> getOuterConstraints(X context);

    <X extends TypeSystemContext<X>> AbstractConstraints<X> getConstraints(X context);
}
