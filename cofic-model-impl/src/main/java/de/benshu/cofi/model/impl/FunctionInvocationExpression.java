package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.AstNodeConstructorMethod;

public class FunctionInvocationExpression<X extends ModelContext<X>> extends ExpressionNode<X> implements FunctionInvocation<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> FunctionInvocationExpression<X> of(ExpressionNode<X> primary, ImmutableList<ExpressionNode<X>> args) {
        return new FunctionInvocationExpression<>(primary, args);
    }

    public final ExpressionNode<X> primary;
    public final ImmutableList<ExpressionNode<X>> args;

    private FunctionInvocationExpression(ExpressionNode<X> primary, ImmutableList<ExpressionNode<X>> args) {
        this.primary = primary;
        this.args = args;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitFunctionInvocationExpression(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> E accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformFunctionInvocationExpression(this);
    }

    @Override
    public ImmutableList<ExpressionNode<X>> getArgs() {
        return args;
    }
}
