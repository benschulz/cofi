package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeMixin;

import java.util.Optional;
import java.util.function.Function;
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
        return visitor.visitUserDefinedStatement(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> S accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformUserDefinedStatement(this);
    }

    @Override
    public ImmutableList<?> getSymbols() {
        return symbols;
    }

    @Override
    public Stream<TransformedUserDefinedNode<X, Statement<X>>> transform(X context, Function<String, TypeMixin<X, ?>> resolve) {
        return transformations.stream()
                .map(t -> t.apply(context, this, resolve))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(TransformedUserDefinedNode::new);
    }
}
