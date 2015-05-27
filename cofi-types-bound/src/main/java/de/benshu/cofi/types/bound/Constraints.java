package de.benshu.cofi.types.bound;

public interface Constraints<X> {
    interface None<X> extends Constraints<X> {
    }

    /**
     * Determines the additional constraints required on contained type variables to have {@code subtype} be
     * a subtype of {@code supertype}.
     *
     * @return {@code subtype <: supertype}
     */
    Constraints<X> establishSubtype(Type<X, ?> subtype, Type<X, ?> supertype);

    /**
     * @param subtypes
     * @param supertypes
     * @return {@code true} iff {@code subtypes[i]} <: {@code supertypes[i]}, for all valid {@code i}s
     */
    boolean areSubtypes(TypeList<X, ?> subtypes, TypeList<X, ?> supertypes);

    boolean isSubtype(Type<X, ?> subtype, Type<X, ?> supertype);
}
