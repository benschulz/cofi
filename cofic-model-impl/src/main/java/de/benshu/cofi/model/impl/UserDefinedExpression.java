package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;

import java.util.stream.Stream;

public class UserDefinedExpression<X extends ModelContext<X>> extends ExpressionNode<X> implements UserDefinedNode<X> {
    private final ImmutableList<?> symbols;
    private final ImmutableList<UserDefinedNodeTransformation<X, ? super UserDefinedExpression<X>, ExpressionNode<X>>> transformations;

    public UserDefinedExpression(ImmutableList<?> symbols, ImmutableList<UserDefinedNodeTransformation<X, ? super UserDefinedExpression<X>, ExpressionNode<X>>> transformations) {
        this.symbols = symbols;
        this.transformations = transformations;
    }

    public UserDefinedExpression(ImmutableList<?> symbols, UserDefinedNodeTransformation<X, ? super UserDefinedExpression<X>, ExpressionNode<X>> transformation) {
        this(symbols, ImmutableList.of(transformation));
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitUserDefinedExpressionNode(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> E accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformUserDefinedExpressionNode(this);
    }

    @Override
    public Object getSymbol(int index) {
        return symbols.get(index);
    }

    @Override
    public Stream<TransformedUserDefinedNode<X, ExpressionNode<X>>> transform() {
        return transformations.stream()
                .map(t -> new TransformedUserDefinedNode<>(t.apply(this)));
    }
}
