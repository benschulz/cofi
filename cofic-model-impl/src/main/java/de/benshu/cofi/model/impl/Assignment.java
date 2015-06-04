package de.benshu.cofi.model.impl;

import de.benshu.cofi.parser.AstNodeConstructorMethod;

public class Assignment<X extends ModelContext<X>> extends Statement<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> Assignment<X> of(ExpressionNode<X> lhs, ExpressionNode<X> rhs) {
        return new Assignment<>(lhs, rhs);
    }

    public final ExpressionNode<X> lhs;
    public final ExpressionNode<X> rhs;

    private Assignment(ExpressionNode<X> lhs, ExpressionNode<X> rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitAssignment(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> S accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformAssignment(this);
    }
}
