package de.benshu.cofi.model.impl;

public class RootExpression<X extends ModelContext<X>> extends ExpressionNode<X> {
    public static <X extends ModelContext<X>> RootExpression<X> of() {
        return new RootExpression<>();
    }

    private RootExpression() {}

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitRootExpression(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> E accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformRootExpression(this);
    }

}
