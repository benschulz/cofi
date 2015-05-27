package de.benshu.cofi.types.bound;

public interface ConstructedUnionType<X, S extends ConstructedUnionType<X, S, C>, C extends UnionTypeConstructor<X, C, S>>
        extends UnionType<X, S> {
}
