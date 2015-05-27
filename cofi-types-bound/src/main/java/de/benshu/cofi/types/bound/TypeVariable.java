package de.benshu.cofi.types.bound;

public interface TypeVariable<X, S extends TypeVariable<X, S>> extends Type<X, S> {

    TypeParameterList<X> getParameterList();

    TypeParameter<X> getParameter();

}
