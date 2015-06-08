package de.benshu.cofi.model.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class TupleTypeExpression<X extends ModelContext<X>> extends TypeExpression<X> {
    static <X extends ModelContext<X>> TupleTypeExpression<X> create(ImmutableList<TypeExpression<X>> types) {
        return new TupleTypeExpression<>(types);
    }

    public final ImmutableList<TypeExpression<X>> types;

    private TupleTypeExpression(ImmutableList<TypeExpression<X>> types) {
        this.types = types;

        Preconditions.checkArgument(this.types.size() >= 2 || this.types.size() == 0);
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitTupleType(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> T accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformTupleType(this);
    }
}
