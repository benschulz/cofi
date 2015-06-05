package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;

import java.util.stream.Stream;

import static com.google.common.collect.Maps.immutableEntry;

public class UserDefinedExpression<X extends ModelContext<X>> extends ExpressionNode<X> implements UserDefinedNode<X> {
    private final ImmutableList<?> symbols;
    private final ImmutableList<UserDefinedNodeTransformation<X, ? super UserDefinedExpression<X>, ExpressionNode<X>>> transformations;

    public UserDefinedExpression(ImmutableList<?> symbols, ImmutableList<UserDefinedNodeTransformation<X, ? super UserDefinedExpression<X>, ExpressionNode<X>>> transformations) {
        this.symbols = symbols;
        this.transformations = transformations;
    }

    @SafeVarargs
    public UserDefinedExpression(ImmutableList<?> symbols, UserDefinedNodeTransformation<X, ? super UserDefinedExpression<X>, ExpressionNode<X>>... transformations) {
        this(symbols, ImmutableList.copyOf(transformations));
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitUserDefinedExpression(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> E accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformUserDefinedExpression(this);
    }

    @Override
    public ImmutableList<?> getSymbols() {
        return symbols;
    }

    @Override
    public Stream<TransformedUserDefinedNode<X, ExpressionNode<X>>> transform(TransformationContext<X> context) {
        return transformations.stream()
                .map(t -> immutableEntry(t, t.apply(context, this)))
                .filter(e -> e.getValue().isPresent())
                .map(e -> new TransformedUserDefinedNode<>(
                        e.getValue().get(),
                        (x, t) -> e.getKey().test(x, t)));
    }
}
