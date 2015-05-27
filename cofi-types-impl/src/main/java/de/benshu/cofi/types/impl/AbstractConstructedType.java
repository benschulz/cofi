package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.impl.lists.AbstractTypeList;

public abstract class AbstractConstructedType<X extends TypeSystemContext<X>, S extends AbstractConstructedType<X, S, C>, C extends TypeConstructorMixin<X, C, S>>
        extends AbstractProperType<X, S>
        implements ConstructedTypeMixin<X, S, C>, Substitutable<X, S> {
    private final C constructor;
    private final AbstractTypeList<X, ?> arguments;

    protected AbstractConstructedType(C constructor, AbstractTypeList<X, ?> arguments) {
        super(constructor.getContext());

        this.constructor = constructor;
        this.arguments = arguments;
    }

    @Override
    public S substitute(Substitutions<X> substitutions) {
        if (substitutions.isEmpty()) {
            @SuppressWarnings("unchecked")
            S self = (S) this;
            return self;
        }

        AbstractTypeList<X, ?> args = getArguments().substitute(substitutions);
        return getConstructor().apply(args);
    }

    @Override
    public AbstractTypeList<X, ?> getArguments() {
        return arguments;
    }

    @Override
    public C getConstructor() {
        return constructor;
    }

    @Override
    public String toDescriptor() {
        return ConstructedTypeMixin.super.toDescriptor();
    }
}
