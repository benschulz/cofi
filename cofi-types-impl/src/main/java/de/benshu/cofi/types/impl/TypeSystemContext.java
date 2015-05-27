package de.benshu.cofi.types.impl;

import de.benshu.cofi.cofic.notes.async.Checker;

public interface TypeSystemContext<X extends TypeSystemContext<X>> {
    TypeSystemImpl<X> getTypeSystem();

    Checker getChecker();
}
