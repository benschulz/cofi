package de.benshu.cofi.model.impl;

public interface TypeParameterized<X extends ModelContext<X>> {
    TypeParameters<X> getTypeParameters();
}
