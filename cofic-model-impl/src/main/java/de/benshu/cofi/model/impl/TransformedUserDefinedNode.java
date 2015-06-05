package de.benshu.cofi.model.impl;

import java.util.function.BiPredicate;

public class TransformedUserDefinedNode<X extends ModelContext<X>, T extends ModelNodeMixin<X>> {
    private final T transformedNode;
    private final BiPredicate<TransformationContext<X>, T> test;

    public TransformedUserDefinedNode(T transformedNode, BiPredicate<TransformationContext<X>, T> test) {
        this.transformedNode = transformedNode;
        this.test = test;
    }

    public T getTransformedNode() {
        return transformedNode;
    }

    public BiPredicate<TransformationContext<X>, T> unboundTest() {
        return test;
    }

    public <U> BiPredicate<TransformationContext<X>, U> boundTest() {
        return (x, t) -> test.test(x, transformedNode);
    }

    public boolean test(TransformationContext<X> context) {
        return test.test(context, transformedNode);
    }
}
