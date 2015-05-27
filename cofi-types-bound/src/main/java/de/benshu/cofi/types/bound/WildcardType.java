package de.benshu.cofi.types.bound;

public interface WildcardType<X> extends ProperType<X, WildcardType<X>> {

    TypeParameter<X> getParam();

}
