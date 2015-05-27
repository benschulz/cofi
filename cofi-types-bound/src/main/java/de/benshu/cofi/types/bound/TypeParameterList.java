package de.benshu.cofi.types.bound;

public interface TypeParameterList<X> extends Iterable<TypeParameter<X>> {
    int size();

    boolean isEmpty();

    TypeParameter<X> get(int index);

    Constraints<X> getConstraints();
}
