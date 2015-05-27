package de.benshu.cofi.types.bound;

public interface ConstructedType<X, S extends ConstructedType<X, S, C>, C extends TypeConstructor<X, C, S>>
        extends ProperType<X, S> {

    C getConstructor();

    TypeList<X, ?> getArguments();
}
