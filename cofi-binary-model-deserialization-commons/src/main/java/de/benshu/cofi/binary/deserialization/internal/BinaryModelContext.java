package de.benshu.cofi.binary.deserialization.internal;

import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;

public interface BinaryModelContext<X extends BinaryModelContext<X>> extends TypeSystemContext<X> {
    TypeMixin<X, ?> resolveQualifiedTypeName(Fqn fqn);
}
