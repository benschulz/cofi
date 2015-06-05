package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeMixin;

import java.util.function.Function;
import java.util.stream.Stream;

public interface UserDefinedNode<X extends ModelContext<X>> extends ModelNodeMixin<X> {
    ImmutableList<?> getSymbols();

    default Object getSymbol(int index) {
        return getSymbols().get(index);
    }

    Stream<? extends TransformedUserDefinedNode<X, ? extends ModelNodeMixin<X>>> transform(TransformationContext<X> context);
}
