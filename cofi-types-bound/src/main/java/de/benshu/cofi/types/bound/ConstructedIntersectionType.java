package de.benshu.cofi.types.bound;

public interface ConstructedIntersectionType<X, S extends ConstructedIntersectionType<X, S, C>, C extends IntersectionTypeConstructor<X, C, S>>
        extends IntersectionType<X, S> {
}
