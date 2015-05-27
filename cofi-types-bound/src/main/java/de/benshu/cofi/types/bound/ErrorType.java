package de.benshu.cofi.types.bound;

/**
 * A well-typed program will never contain an error type.
 */
public interface ErrorType<X, S extends ErrorType<X, S>> extends ProperType<X, S> {}
