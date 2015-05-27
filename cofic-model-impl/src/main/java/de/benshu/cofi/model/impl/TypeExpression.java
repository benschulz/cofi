package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.AstNodeConstructorMethod;

public abstract class TypeExpression<X extends ModelContext<X>> extends AbstractModelNode<X> {
    public static <X extends ModelContext<X>> ImmutableList<TypeExpression<X>> none() {
        return ImmutableList.of();
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> TypeExpression<X> of(ImmutableList<TypeExpression<X>> types) {
        return types.size() == 1 ? types.get(0) : TupleTypeExpression.create(types);
    }

    @Override
    public abstract <N, L extends N, D extends L, S extends N, E extends N, T extends E> T accept(ModelTransformer<X, N, L, D, S, E, T> transformer);
}
