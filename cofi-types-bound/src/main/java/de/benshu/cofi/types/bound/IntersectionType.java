package de.benshu.cofi.types.bound;

public interface IntersectionType<X, S extends IntersectionType<X, S>>
        extends ProperType<X, S> {

    TypeList<X, ? extends ProperType<X, ?>> getElements();
}
