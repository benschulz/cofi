package de.benshu.cofi.types.impl;

public abstract class AbstractType<X extends TypeSystemContext<X>, S extends TypeMixin<X, S>> implements TypeMixin<X, S> {
    private final X context;

    AbstractType(X context) {
        this.context = context;
    }

    @Override
    public final String toString() {
        return debug();
    }

    @Override
    public X getContext() {
        return context;
    }
}
