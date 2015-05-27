package de.benshu.cofi.types.bound;

public interface ProperTypeVariable<X, S extends ProperTypeVariable<X, S>>
        extends TypeVariable<X, S>, ProperType<X, S> {
}
