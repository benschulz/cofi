package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;

import java.util.stream.Stream;

public class UserDefinedStatement<X extends ModelContext<X>>
        extends Statement<X>
        implements UserDefinedNode<X> {

    private final ImmutableList<?> symbols;
    private final ImmutableList<UserDefinedNodeTransformation<X, ? super UserDefinedStatement<X>, Statement<X>>> transformations;

    public UserDefinedStatement(ImmutableList<?> symbols, ImmutableList<UserDefinedNodeTransformation<X, ? super UserDefinedStatement<X>, Statement<X>>> transformations) {
        this.symbols = symbols;
        this.transformations = transformations;
    }

    public UserDefinedStatement(ImmutableList<?> symbols, UserDefinedNodeTransformation<X, ? super UserDefinedStatement<X>, Statement<X>> transformation) {
        this(symbols, ImmutableList.of(transformation));
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitUserDefinedStatementNode(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> S accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformUserDefinedStatementNode(this);
    }

    @Override
    public Object getSymbol(int index) {
        return symbols.get(index);
    }

    public Stream<TransformedUserDefinedNode<X, Statement<X>>> transform() {
        return transformations.stream()
                .map(t -> new TransformedUserDefinedNode<>(t.apply(this)));
    }
}
