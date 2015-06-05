package de.benshu.cofi.model.impl;

import de.benshu.cofi.model.Expression;

public interface ExpressionMixin<X extends ModelContext<X>> extends ModelNodeMixin<X>, Expression<X> {
    @Override
    <N, L extends N, D extends L, S extends N, E extends N, T extends E> E accept(ModelTransformer<X, N, L, D, S, E, T> transformer);
}
