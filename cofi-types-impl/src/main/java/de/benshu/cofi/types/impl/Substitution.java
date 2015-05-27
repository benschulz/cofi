package de.benshu.cofi.types.impl;

public class Substitution<X extends TypeSystemContext<X>> {
    public static <X extends TypeSystemContext<X>> Substitution<X> ofThrough(TypeVariableImpl<X, ?> variable, TypeMixin<X, ?> type) {
        return new Substitution<>(variable, type);
    }

    private final TypeVariableImpl<X, ?> variable;
    private final TypeMixin<X, ?> substitute;

    private Substitution(TypeVariableImpl<X, ?> variable, TypeMixin<X, ?> type) {
        this.variable = variable;
        this.substitute = type;
    }

    public TypeVariableImpl<X, ?> getVariable() {
        return variable;
    }

    public TypeMixin<X, ?> getSubstitute() {
        return substitute;
    }
}
