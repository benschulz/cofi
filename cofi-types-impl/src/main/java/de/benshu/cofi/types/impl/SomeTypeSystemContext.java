package de.benshu.cofi.types.impl;

import de.benshu.cofi.cofic.notes.async.Checker;

public class SomeTypeSystemContext implements TypeSystemContext<SomeTypeSystemContext> {
    private SomeTypeSystemContext(){
        throw new AssertionError();
    }

    @Override
    public TypeSystemImpl<SomeTypeSystemContext> getTypeSystem() {
        throw new AssertionError();
    }

    @Override
    public Checker getChecker() {
        throw new AssertionError();
    }
}
