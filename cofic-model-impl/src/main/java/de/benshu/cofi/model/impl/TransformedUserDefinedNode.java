package de.benshu.cofi.model.impl;

public class TransformedUserDefinedNode<X extends ModelContext<X>, T extends ModelNodeMixin<X>> {
    private final T transformedNode;

    public TransformedUserDefinedNode(T transformedNode) {
        this.transformedNode = transformedNode;
    }

    public T getTransformedNode() {
        return transformedNode;
    }
}
