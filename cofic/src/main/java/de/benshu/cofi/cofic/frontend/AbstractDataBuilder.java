package de.benshu.cofi.cofic.frontend;

public abstract class AbstractDataBuilder<S extends AbstractDataBuilder<S, D>, D> {
    protected AbstractDataBuilder() { }

    public abstract S addAll(S other);

    public abstract D build();
}
