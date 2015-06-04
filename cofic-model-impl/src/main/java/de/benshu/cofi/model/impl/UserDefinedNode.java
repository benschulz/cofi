package de.benshu.cofi.model.impl;

import java.util.stream.Stream;

public interface UserDefinedNode<X extends ModelContext<X>> extends ModelNodeMixin<X> {
    Object getSymbol(int index);

    Stream<? extends TransformedUserDefinedNode<X, ? extends ModelNodeMixin<X>>> transform();
}
