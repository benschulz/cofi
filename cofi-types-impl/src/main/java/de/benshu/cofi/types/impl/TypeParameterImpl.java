package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.TypeParameterList;
import de.benshu.cofi.types.TypeVariable;
import de.benshu.cofi.types.Variance;
import de.benshu.cofi.types.bound.TypeParameter;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.exception.UnexpectedBranchException;

import java.util.function.Function;

public class TypeParameterImpl<X extends TypeSystemContext<X>> implements TypeParameter<X> {
    private final TypeParameterListImpl<X> list;
    private final int index;
    private final Variance variance;
    private final Tags tags;
    private final TypeVariableImpl<X, ?> variable;

    TypeParameterImpl(TypeParameterListImpl<X> list, int index, Variance variance, IndividualTags tags, Function<TypeParameterImpl<X>, TypeVariableImpl<X, ?>> variableConstructor) {
        this.list = list;
        this.index = index;
        this.variance = variance;
        this.tags = HashTags.create(this, tags);
        this.variable = variableConstructor.apply(this);
    }

    public Tags getTags() {
        return tags;
    }

    @Override
    public TypeVariableImpl<X, ?> getVariable() {
        return variable;
    }

    @Override
    public Variance getVariance() {
        return variance;
    }

    public boolean isContravariant() {
        return variance.isContravariant();
    }

    public boolean isCovariant() {
        return variance.isCovariant();
    }

    @Override
    public String debug() {
        return toDescriptor();
    }

    public String toDescriptor() {
        final String variableDescriptor = getVariable().toDescriptor();

        switch (getVariance()) {
            case BIVARIANT:
                return "+-" + variableDescriptor;
            case CONTRAVARIANT:
                return "-" + variableDescriptor;
            case COVARIANT:
                return "+" + variableDescriptor;
            case INVARIANT:
                return variableDescriptor;
            default:
                throw new UnexpectedBranchException();
        }
    }

    @Override
    public String toString() {
        return debug();
    }

    @Override
    public TypeParameterListImpl<X> getList() {
        return list;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public Unbound<X> unbind() {
        return new Unbound<>(this);
    }

    private static class Unbound<X extends TypeSystemContext<X>> implements de.benshu.cofi.types.TypeParameter {
        private final TypeParameterImpl<X> unbound;

        public Unbound(TypeParameterImpl<X> unbound) {
            this.unbound = unbound;
        }

        @Override
        public TypeParameterList getList() {
            return unbound.getList().unbind();
        }

        @Override
        public TypeVariable getVariable() {
            return unbound.getVariable().unbind();
        }

        @Override
        public Variance getVariance() {
            return unbound.getVariance();
        }

        @Override
        public int getIndex() {
            return unbound.getIndex();
        }
    }
}
