package de.benshu.cofi.types.bound;

import de.benshu.cofi.types.Kind;
import de.benshu.commons.core.Debuggable;

public interface Type<X, S extends Type<X, S>> extends Debuggable, de.benshu.cofi.types.tags.Taggable<S> {
    String debug();

    Kind getKind();
}
