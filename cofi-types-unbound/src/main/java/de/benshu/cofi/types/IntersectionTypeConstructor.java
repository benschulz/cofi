package de.benshu.cofi.types;

public interface IntersectionTypeConstructor extends ProperTypeConstructor<ConstructedIntersectionType> {
    TypeList<? extends ProperTypeConstructor<?>> getElements();
}
