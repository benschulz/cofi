package de.benshu.cofi.model.impl;


import de.benshu.cofi.parser.AstNodeConstructorMethod;

public class NamedTypeExpression<X extends ModelContext<X>> extends TypeExpression<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> NamedTypeExpression<X> of2(NameImpl<X> name) {
        return new NamedTypeExpression<>(name);
    }

    public final NameImpl<X> name;

    private NamedTypeExpression(NameImpl<X> name) {
        this.name = name;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitNamedType(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> T accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformNamedType(this);
    }
}
