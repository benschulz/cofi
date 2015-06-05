package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.AstNodeConstructorMethod;

public class TypeBody<X extends ModelContext<X>> extends AbstractModelNode<X> {
    public static abstract class Element<X extends ModelContext<X>> extends AbstractModelNode<X> {
        public static <X extends ModelContext<X>> ImmutableList<Element<X>> none() {
            return ImmutableList.of();
        }

        @Override
        public abstract <N, L extends N, D extends L, S extends N, E extends N, T extends N> L accept(ModelTransformer<X, N, L, D, S, E, T> transformer);
    }

    public static <X extends ModelContext<X>> TypeBody<X> empty() {
        return of(ImmutableList.of());
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> TypeBody<X> of(ImmutableList<Element<X>> elements) {
        return new TypeBody<>(elements);
    }

    public final ImmutableList<Element<X>> elements;

    public TypeBody(ImmutableList<Element<X>> elements) {
        this.elements = elements;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitTypeBody(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformTypeBody(this);
    }

}
