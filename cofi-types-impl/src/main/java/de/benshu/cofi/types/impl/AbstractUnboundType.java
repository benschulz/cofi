package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.Kind;
import de.benshu.cofi.types.Type;
import de.benshu.cofi.types.tags.Tags;

public abstract class AbstractUnboundType<X extends TypeSystemContext<X>, U extends TypeMixin<X, ?>> implements Type {
    public final U bound;

    public AbstractUnboundType(U bound) {
        this.bound = bound;
    }

    @Override
    public final String debug() {
        return bound.debug();
    }

    @Override
    public final Kind getKind() {
        return bound.getKind();
    }

    @Override
    public final Tags getTags() {
        return bound.getTags();
    }

    public final <Y extends TypeSystemContext<Y>> AbstractUnboundType<Y, ?> rebind() {
        return (AbstractUnboundType<Y, ?>) this;
    }

    @Override
    public String toString() {
        return debug();
    }
}
