package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;

public class FunctionInvocationStatement<X extends ModelContext<X>> extends Statement<X> {
    public static <X extends ModelContext<X>> FunctionInvocationStatement<X> of(ImmutableList<AnnotationImpl<X>> annotations, NameImpl<X> name, ImmutableList<ExpressionNode<X>> arguments) {
        return new FunctionInvocationStatement<>(annotations, name, arguments);
    }

    public final ImmutableList<AnnotationImpl<X>> annotations;
    public final NameImpl<X> name;
    public final ImmutableList<ExpressionNode<X>> arguments;

    public FunctionInvocationStatement(ImmutableList<AnnotationImpl<X>> annotations, NameImpl<X> name, ImmutableList<ExpressionNode<X>> arguments) {
        this.annotations = annotations;
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitFunctionInvocationStatement(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> S accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformFunctionInvocationStatement(this);
    }
}
