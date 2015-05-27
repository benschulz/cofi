package de.benshu.cofi.types;

public interface UnionType extends ProperType {

    TypeList<? extends ProperType> getElements();

}
