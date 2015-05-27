package de.benshu.cofi.types.bound;

public interface UnionTypeConstructor<X, S extends UnionTypeConstructor<X, S, T>, T extends ConstructedUnionType<X, T, S>>
        extends TypeConstructor<X, S, T> {

    TypeList<X, ? extends TypeConstructor<X, ?, ? extends ProperType<X, ?>>> getElements();
}
