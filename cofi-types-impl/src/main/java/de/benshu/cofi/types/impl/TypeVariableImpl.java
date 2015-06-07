package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.bound.TypeVariable;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Debuggable;

import java.util.Collection;

public abstract class TypeVariableImpl<X extends TypeSystemContext<X>, S extends TypeVariableImpl<X, S>> extends AbstractType<X, S> implements TypeVariable<X, S> {
    private final TypeParameterListImpl<X> parameterList;
    private final TypeParameterImpl<X> parameter;
    private final Tags tags;

    protected TypeVariableImpl(TypeParameterListImpl<X> parameterList, TypeParameterImpl<X> parameter, Tagger tagger) {
        super(null);

        this.parameterList = parameterList;
        this.parameter = parameter;
        this.tags = tagger.tag(this);
    }

    @Override
    public boolean isSameAs(TypeMixin<X, ?> other) {
        return this == other
                || other instanceof TypeVariableImpl<?, ?>
                && isSameAs((TypeVariableImpl<?, ?>) other);
    }

    private boolean isSameAs(TypeVariableImpl<?, ?> other) {
        return getParameterList().getUnbound() == other.getParameterList().getUnbound()
                && getParameter().getIndex() == other.getParameter().getIndex();
    }

    @Override
    public X getContext() {
        return getParameterList().getContext();
    }

    @Override
    public KindImpl getKind() {
        throw null;
    }

    @Override
    public final TypeParameterImpl<X> getParameter() {
        return parameter;
    }

    @Override
    public TypeParameterListImpl<X> getParameterList() {
        return parameterList;
    }

    @Override
    public TypeMixin<X, ?> substitute(Substitutions<X> substitutions) {
        return substitutions.substitute(this);
    }

    @Override
    public String debug() {
        return getParameter().getTags().tryGet(getContext().getTypeSystem().getNameTag())
                .map(Debuggable::debug)
                .getOrSupply(this::fallbackName);
    }

    @Override
    public String toDescriptor() {
        return getParameter().getTags().tryGet(getContext().getTypeSystem().getNameTag())
                .map(Debuggable::debug)
                .getOrThrow(new UnsupportedOperationException());
    }

    private String fallbackName() {
        int size = parameterList.size();
        int index = getParameter().getIndex();

        return size > Character.MAX_RADIX - 10 ? "@T" + (index + 1) : "@" + Integer.toString(index + 10, Character.MAX_RADIX).toUpperCase();
    }

    @Override
    public boolean containsAny(Collection<TypeVariableImpl<X, ?>> variables) {
        return variables.contains(this);
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public de.benshu.cofi.types.TypeVariable unbind() {
        throw null;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof TypeVariableImpl<?, ?> && isSameAs((TypeVariableImpl<?, ?>) obj);
    }

    @Override
    public int hashCode() {
        return getParameter().getIndex();
    }
}
