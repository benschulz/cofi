package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.bound.ConstructedType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

import java.util.Collection;

public interface ConstructedTypeMixin<X extends TypeSystemContext<X>, S extends ProperTypeMixin<X, S> & ConstructedTypeMixin<X, S, C>, C extends TypeConstructorMixin<X, C, S>>
        extends ProperTypeMixin<X, S>, ConstructedType<X, S, C> {

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
        String name = getConstructor().getTags().getOrFallbackToDefault(getContext().getTypeSystem().getNameTag()).debug();
        String arguments = getArguments().toDescriptor();
        return name + arguments;
    }

    @Override
    default boolean containsAny(Collection<TypeVariableImpl<X, ?>> variables) {
        return getArguments().stream().anyMatch(a -> a.containsAny(variables));
    }

}
