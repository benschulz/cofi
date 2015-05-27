package de.benshu.cofi.types;

import de.benshu.commons.core.Debuggable;

public interface TypeParameterList extends Debuggable, Iterable<TypeParameter> {
    int size();

    boolean isEmpty();

    TypeParameter get(int index);

    Constraints getConstraints();
}
