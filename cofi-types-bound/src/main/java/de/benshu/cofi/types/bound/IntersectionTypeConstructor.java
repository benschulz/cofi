package de.benshu.cofi.types.bound;

public interface IntersectionTypeConstructor<X, S extends IntersectionTypeConstructor<X, S, T>, T extends ConstructedIntersectionType<X, T, S>>
        extends TypeConstructor<X, S, T> {

    TypeList<X, ? extends TypeConstructor<X, ?, ? extends ProperType<X, ?>>> getElements();
}
