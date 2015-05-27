package de.benshu.cofi.types.impl;

public interface NamedProperTypeConstructorMixin<X extends TypeSystemContext<X>, S extends NamedProperTypeConstructorMixin<X, S, T>, T extends ProperTypeMixin<X, ?>>
        extends ProperTypeConstructorMixin<X, S, T> {

    @Override
    default String toDescriptor() {
        return getTags().get(getContext().getTypeSystem().getNameTag()).debug();
    }
}
