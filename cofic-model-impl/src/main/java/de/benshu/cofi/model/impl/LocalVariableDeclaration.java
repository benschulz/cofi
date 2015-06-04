package de.benshu.cofi.model.impl;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;

public class LocalVariableDeclaration<X extends ModelContext<X>> extends Statement<X> implements AnnotatedNodeMixin<X>, ModelNodeMixin<X>, de.benshu.cofi.model.ModelNode<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> LocalVariableDeclaration<X> of(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers,
                                                                             Token name, TypeExpression<X> type, ExpressionNode<X> value) {
        return new LocalVariableDeclaration<>(annotations, modifiers, name, type, value);
    }

    public final ImmutableList<AnnotationImpl<X>> annotations;
    public final ImmutableList<ModifierImpl<X>> modifiers;
    public final Token name;
    public final TypeExpression<X> type;
    public final ExpressionNode<X> value;

    private final ImmutableSet<AnnotationImpl<X>> allAnnotations;
    private int index = -1;

    public LocalVariableDeclaration(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers, Token name, TypeExpression<X> type, ExpressionNode<X> value) {
        this.annotations = annotations;
        this.modifiers = modifiers;
        this.allAnnotations = FluentIterable.from(annotations).append(modifiers).toSet();
        this.name = name;
        this.type = type;
        this.value = value == null ? null : value;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitLocalVariableDeclaration(this, aggregate);
    }

    @Override
    public ImmutableSet<AnnotationImpl<X>> getAnnotationsAndModifiers() {
        return allAnnotations;
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> S accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformLocalVariableDeclaration(this);
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name.getLexeme();
    }

    public void setIndex(int index) {
        assert index >= 0;

        this.index = index;
    }

}
