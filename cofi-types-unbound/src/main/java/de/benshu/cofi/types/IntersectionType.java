package de.benshu.cofi.types;

public interface IntersectionType extends ProperType {
    TypeList<? extends ProperType> getElements();
}
