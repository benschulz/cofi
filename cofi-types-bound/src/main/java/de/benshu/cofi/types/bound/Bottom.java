package de.benshu.cofi.types.bound;

public interface Bottom<X, S extends Bottom<X, S>> extends ProperType<X, S> {}
