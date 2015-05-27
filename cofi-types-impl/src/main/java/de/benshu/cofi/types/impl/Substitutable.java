package de.benshu.cofi.types.impl;

public interface Substitutable<X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> {
    // TODO this would go away with declaration site variance
    @SuppressWarnings("unchecked")
    static <X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> Substitutable<X, T> unchecked(TypeMixin<X, ?> type) {
        return new Substitutable<X, T>() {
            @Override
            public T substitute(Substitutions<X> substitutions) {
                return (T) type.substitute(substitutions);
            }

            @Override
            public String toDescriptor() {
                return type.toDescriptor();
            }
        };
    }

    T substitute(Substitutions<X> substitutions);

    String toDescriptor();
}
