package de.benshu.cofi.types.impl.test.matcher;

import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.test.TestContext;

import java.util.function.Function;

public class InContextPreparation<T> {
    private final Function<AbstractConstraints<TestContext>, T> constructor;

    public InContextPreparation(Function<AbstractConstraints<TestContext>, T> constructor) {
        this.constructor = constructor;
    }

    public T given(AbstractConstraints<TestContext> constraints) {
        return constructor.apply(constraints);
    }

    public T givenNoConstraints() {
        return given(AbstractConstraints.none());
    }
}
