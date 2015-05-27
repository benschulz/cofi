package de.benshu.cofi.types;

public interface Constraints {
    /**
     * Determines the additional constraints required on contained type variables to have {@code subtype} be
     * a subtype of {@code supertype}.
     *
     * @return {@code subtype <: supertype}
     */
    Constraints establishSubtype(Type subtype, Type supertype);

    /**
     * @param subtypes
     * @param supertypes
     * @return {@code true} iff {@code subtypes[i]} <: {@code supertypes[i]}, for all valid {@code i}s
     */
    boolean areSubtypes(TypeList<?> subtypes, TypeList<?> supertypes);

    boolean isSubtype(Type subtype, Type supertype);

    boolean areEqualTypes(Type a, Type b);
}
