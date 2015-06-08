package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.parser.AstNodeConstructorMethod;

public class ExpressionStatement<X extends ModelContext<X>> extends Statement<X> implements AnnotatedNodeMixin<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> ExpressionStatement<X> of(ImmutableList<AnnotationImpl<X>> annotations, ExpressionNode<X> expression) {
        return new ExpressionStatement<>(annotations, expression);
    }

    public final ImmutableList<AnnotationImpl<X>> annotations;
    public final ExpressionNode<X> expression;

    private ExpressionStatement(ImmutableList<AnnotationImpl<X>> annotations, ExpressionNode<X> expression) {
        this.annotations = annotations;
        this.expression = expression;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitExpressionStatement(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> S accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformExpressionStatement(this);
    }

    @Override
    public ImmutableSet<AnnotationImpl<X>> getAnnotationsAndModifiers() {
        return ImmutableSet.copyOf(annotations);
    }

    @Override
    public String toString() {
        return expression.toString();
    }
}
