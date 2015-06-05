package de.benshu.cofi.types.impl;

import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

import static com.google.common.base.Preconditions.checkArgument;

public class TypeConstructorInvocation<X extends TypeSystemContext<X>> {
    private final TypeConstructorMixin<X, ?, ?> typeConstructor;
    private final ImmutableMap<AbstractTypeList<X, ?>, TypeMixin<X, ?>> invocations;

    public TypeConstructorInvocation(TypeConstructorMixin<X, ?, ?> typeConstructor, AbstractTypeList<X, ?> arguments, TypeMixin<X, ?> constructed) {
        this.typeConstructor = typeConstructor;
        this.invocations = ImmutableMap.of(arguments, constructed);
    }

    private TypeConstructorInvocation(TypeConstructorMixin<X, ?, ?> typeConstructor, ImmutableMap<AbstractTypeList<X, ?>, TypeMixin<X, ?>> invocations) {
        this.typeConstructor = typeConstructor;
        this.invocations = invocations;
    }

    public TypeConstructorInvocation<X> combine(TypeConstructorInvocation<X> other) {
        checkArgument(typeConstructor.isSameAs(other.typeConstructor));

        return new TypeConstructorInvocation<>(
                typeConstructor,
                ImmutableMap.<AbstractTypeList<X, ?>, TypeMixin<X, ?>>builder()
                        .putAll(invocations)
                        .putAll(other.invocations)
                        .build()
        );
    }
}
