package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.bound.ConstructedType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

import java.util.Collection;
import java.util.Optional;

public interface ConstructedTypeMixin<X extends TypeSystemContext<X>, S extends ProperTypeMixin<X, S> & ConstructedTypeMixin<X, S, C>, C extends TypeConstructorMixin<X, C, S>>
        extends ProperTypeMixin<X, S>, ConstructedType<X, S, C> {

    @Override
    default boolean isSameAs(TypeMixin<X, ?> other) {
        if(this == other)
            return true;
        if(!(other instanceof ConstructedTypeMixin<?,?,?>))
            return false;

        ConstructedTypeMixin<X,?,?> otherConstructed = (ConstructedTypeMixin<X,?,?>)other;

        if(!getConstructor().isSameAs(otherConstructed.getConstructor()))
            return false;

        AbstractTypeList<X, ?> myArgs = getArguments();
        AbstractTypeList<X, ?> othersArgs = otherConstructed.getArguments();

        for (int i = 0; i < myArgs.size(); ++i)
            if (!myArgs.get(i).isSameAs(othersArgs.get(i)))
                return false;

        return true;
    }

    @Override
    default S substitute(Substitutions<X> substitutions) {
        return getConstructor().apply(getArguments().substitute(substitutions));
    }

    @Override
    AbstractTypeList<X, ?> getArguments();

    default Substitutions<X> getArgumentsAsSubstitutions() {
        return Substitutions.ofThrough(getConstructor().getParameters(), getArguments());
    }

    @Override
    C getConstructor();

    @Override
    default String debug() {
        String name = getConstructor().getTags().getOrFallbackToDefault(getContext().getTypeSystem().getNameTag()).debug();
        String arguments = getArguments().debug();
        return name + arguments;
    }

    @Override
    default String toDescriptor() {
        String name = getConstructor().getTags().getOrFallbackToDefault(getContext().getTypeSystem().getNameTag()).toDescriptor();
        String arguments = getArguments().toDescriptor();
        return name + arguments;
    }

    @Override
    default boolean containsAny(Collection<TypeVariableImpl<X, ?>> variables) {
        return getArguments().stream().anyMatch(a -> a.containsAny(variables));
    }

    @Override
    default Optional<TypeConstructorInvocation<X>> tryGetInvocationOf(TypeConstructorMixin<X, ?, ?> typeConstructor) {
        return getConstructor().isSameAs(typeConstructor)
                ? Optional.of(new TypeConstructorInvocation(typeConstructor, getArguments(), this))
                : Optional.empty();
    }
}
