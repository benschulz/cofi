package de.benshu.cofi.types.impl.test;

import de.benshu.cofi.cofic.notes.async.Checker;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeSystemImpl;

import javax.inject.Inject;

public class TestContext implements TypeSystemContext<TestContext> {
    private final TypeSystemImpl<TestContext> typeSystem;

    @Inject
    public TestContext(TypeSystemImpl<TestContext> typeSystem) {
        this.typeSystem = typeSystem;
    }

    @Override
    public TypeSystemImpl<TestContext> getTypeSystem() {
        return typeSystem;
    }

    @Override
    public Checker getChecker() {
        return check -> {};
    }
}
