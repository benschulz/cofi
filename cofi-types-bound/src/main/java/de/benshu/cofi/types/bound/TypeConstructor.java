package de.benshu.cofi.types.bound;

public interface TypeConstructor<X, S extends TypeConstructor<X, S, T>, T extends Type<X, ?>> extends Type<X, S> {
    TypeParameterList<X> getParameters();

    /**
     * @param arguments type arguments
     * @return a proper type of value sort
     */
    T apply(TypeList<X, ?> arguments);

}
