package de.benshu.cofi.model.impl;

import java.util.function.Function;

public interface UserDefinedNodeTransformation<X extends ModelContext<X>, U extends UserDefinedNode<X>, T extends ModelNodeMixin<X>> extends Function<U, T> {
    @Override
    T apply(U untransformed);

    boolean test(T transformed, X context);
}
