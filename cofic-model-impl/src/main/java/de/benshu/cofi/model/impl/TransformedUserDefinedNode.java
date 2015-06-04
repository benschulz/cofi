package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableMap;

public class TransformedUserDefinedNode<X extends ModelContext<X>, T extends ModelNodeMixin<X>> {
    private final T transformedNode;
    private final ImmutableMap<ModelNodeMixin<X>, ModelNodeMixin<X>> transformations;

    public TransformedUserDefinedNode(T transformedNode, ImmutableMap<ModelNodeMixin<X>, ModelNodeMixin<X>> transformations) {
        this.transformedNode = transformedNode;
        this.transformations = transformations;
    }

    public TransformedUserDefinedNode(T transformedNode) {
        this(transformedNode, ImmutableMap.of());
    }

    public T getTransformedNode() {
        return transformedNode;
    }

    public ImmutableMap<ModelNodeMixin<X>, ModelNodeMixin<X>> getTransformations() {
        return transformations;
    }
}
