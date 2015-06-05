package de.benshu.cofi.model.impl;


import com.google.common.collect.Iterables;
import de.benshu.cofi.parser.AstNodeConstructorMethod;

public class NameExpression<X extends ModelContext<X>> extends ExpressionNode<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> NameExpression<X> of(NameImpl<X> name) {
        return new NameExpression<>(name);
    }

    public final NameImpl<X> name;

    private NameExpression(NameImpl<X> name) {
        this.name = name;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitNameExpression(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> E accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformNameExpression(this);
    }

    @Override
    public String toString() {
        return Iterables.getOnlyElement(name.ids).getLexeme();
    }
}
