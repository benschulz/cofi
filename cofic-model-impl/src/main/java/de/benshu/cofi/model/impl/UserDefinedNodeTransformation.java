package de.benshu.cofi.model.impl;

import de.benshu.cofi.types.impl.TypeMixin;

import java.util.Optional;
import java.util.function.Function;

public interface UserDefinedNodeTransformation<X extends ModelContext<X>, U extends UserDefinedNode<X>, T extends ModelNodeMixin<X>> {
    Optional<T> apply(X context, U untransformed, Function<String, TypeMixin<X, ?>> resolve);

    boolean test(X context, T transformed);
}
