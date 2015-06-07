package de.benshu.cofi.model.impl;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class TransformedUserDefinedNodes<X extends ModelContext<X>, T extends ModelNodeMixin<X>> {
    public static <X extends ModelContext<X>, T extends ModelNodeMixin<X>> TransformedUserDefinedNodes<X, T> of(Supplier<Stream<TransformedUserDefinedNode<X, T>>> streamSupplier) {
        return new TransformedUserDefinedNodes<>(streamSupplier);
    }

    private final Supplier<Stream<TransformedUserDefinedNode<X, T>>> streamSupplier;

    public TransformedUserDefinedNodes(Supplier<Stream<TransformedUserDefinedNode<X, T>>> streamSupplier) {
        this.streamSupplier = streamSupplier;
    }

    public Stream<TransformedUserDefinedNode<X, T>> stream() {
        return streamSupplier.get();
    }
}
