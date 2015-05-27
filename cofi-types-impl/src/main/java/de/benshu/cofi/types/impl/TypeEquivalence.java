package de.benshu.cofi.types.impl;

import com.google.common.base.Equivalence;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;

public class TypeEquivalence<X extends TypeSystemContext<X>> extends Equivalence<TypeMixin<X, ?>> {
    public static <X extends TypeSystemContext<X>> TypeEquivalence<X> given(AbstractConstraints<X> contextualConstraints) {
        return new TypeEquivalence<>(contextualConstraints);
    }

    private final AbstractConstraints<X> contextualConstraints;

    public TypeEquivalence(AbstractConstraints<X> contextualConstraints) {
        this.contextualConstraints = contextualConstraints;
    }

    @Override
    protected boolean doEquivalent(TypeMixin<X, ?> a, TypeMixin<X, ?> b) {
        return contextualConstraints.areEqualTypes(a, b);
    }

    @Override
    protected int doHash(TypeMixin<X, ?> xTypeMixin) {
        return 0; // …
    }
}
