package de.benshu.cofi.runtime;

import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.TypeConstructor;

public interface NamedEntity extends ModelNode {
    <R> R accept(NamedEntityVisitor<R> visitor);

    String getName();

    TypeConstructor<? extends ProperType> getType();
}
