package de.benshu.cofi.binary.deserialization.internal;

import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.commons.core.Optional;

public interface TypeReferenceContext {
    <X extends BinaryModelContext<X>> Optional<AbstractConstraints<X>> getOuterConstraints(X context);

    <X extends BinaryModelContext<X>> AbstractConstraints<X> getConstraints(X context);
}
