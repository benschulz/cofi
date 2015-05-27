package de.benshu.cofi.types;

public interface UnionTypeConstructor extends ProperTypeConstructor<ConstructedUnionType> {
    TypeList<? extends ProperTypeConstructor<?>> getElements();
}
