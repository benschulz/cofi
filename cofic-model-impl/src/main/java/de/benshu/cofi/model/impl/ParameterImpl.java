package de.benshu.cofi.model.impl;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.model.Parameter;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;

public class ParameterImpl<X extends ModelContext<X>> extends AbstractModelNode<X> implements AnnotatedNodeMixin<X>, Parameter<X>, ModelNodeMixin<X>, de.benshu.cofi.model.ModelNode<X> {
    public static <X extends ModelContext<X>> ImmutableList<ParameterImpl<X>> none() {
        return ImmutableList.of();
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> ParameterImpl<X> of(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers, Token name, TypeExpression<X> type, Token varargs, ExpressionNode<X> defaultValue) {
        return new ParameterImpl<>(annotations, modifiers, name, type, varargs, defaultValue);
    }

    public final ImmutableList<AnnotationImpl<X>> annotations;
    public final ImmutableList<ModifierImpl<X>> modifiers;
    public final Token name;
    public final TypeExpression<X> type;
    public final Token varargs;
    public final ExpressionNode<X> defaultValue;

    private final ImmutableSet<AnnotationImpl<X>> allAnnotations;
    private int index = -1;

    public ParameterImpl(ImmutableList<AnnotationImpl<X>> annotations,
                         ImmutableList<ModifierImpl<X>> modifiers, Token name, TypeExpression<X> type, Token varargs,
                         ExpressionNode<X> defaultValue) {
        this.annotations = annotations;
        this.modifiers = modifiers;
        this.allAnnotations = FluentIterable.from(annotations).append(modifiers).toSet();
        this.name = name;
        this.type = type;
        this.varargs = varargs;
        this.defaultValue = defaultValue == null ? null : defaultValue;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitParameter(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformParameter(this);
    }

    @Override
    public ImmutableSet<AnnotationImpl<X>> getAnnotationsAndModifiers() {
        return allAnnotations;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return name.getLexeme();
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
