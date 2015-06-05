package de.benshu.cofi.model.impl;

import java.util.Optional;

public interface UserDefinedNodeTransformation<X extends ModelContext<X>, U extends UserDefinedNode<X>, T extends ModelNodeMixin<X>> {
    Optional<T> apply(TransformationContext<X> context, U untransformed);

    boolean test(TransformationContext<X> context, T transformed);
}
