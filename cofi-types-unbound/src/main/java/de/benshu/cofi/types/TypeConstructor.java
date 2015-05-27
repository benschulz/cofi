package de.benshu.cofi.types;

public interface TypeConstructor<T extends Type> extends Type {
    TypeParameterList getParameters();

    T applyTrivially();

    T apply(TypeList<?> arguments);
}
