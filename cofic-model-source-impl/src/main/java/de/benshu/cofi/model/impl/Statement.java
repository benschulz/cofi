package de.benshu.cofi.model.impl;

public abstract class Statement<X extends ModelContext<X>> extends AbstractModelNode<X> {
    @Override
    public abstract <N, L extends N, D extends L, S extends N, E extends N, T extends N> S accept(ModelTransformer<X, N, L, D, S, E, T> transformer);
}
