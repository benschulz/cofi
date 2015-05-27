package de.benshu.cofi.types;

public interface ConstructedType<C extends TypeConstructor<?>> extends ProperType {
    TypeList<?> getArguments();

    C getConstructor();
}
