package de.benshu.cofi.model.impl;

public abstract class ExpressionNode<X extends ModelContext<X>> extends AbstractModelNode<X> implements ExpressionMixin<X>, ModelNodeMixin<X>, de.benshu.cofi.model.ModelNode<X> {
    @Override
    public abstract <N, L extends N, D extends L, S extends N, E extends N, T extends E> E accept(ModelTransformer<X, N, L, D, S, E, T> transformer);

}
