package de.benshu.cofi.types.bound;

public interface UnionType<X, S extends UnionType<X, S>> extends ProperType<X, S> {

    TypeList<X, ? extends ProperType<X, ?>> getElements();

}
