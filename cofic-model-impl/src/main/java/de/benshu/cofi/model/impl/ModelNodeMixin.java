package de.benshu.cofi.model.impl;

import de.benshu.cofi.model.ModelNode;

public interface ModelNodeMixin<X extends ModelContext<X>> extends ModelNode<X> {
    <T> T accept(ModelVisitor<X, T> visitor, T aggregate);

    <N, L extends N, D extends L, S extends N, E extends N, T extends E> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer);

    void markSynthetic();
}