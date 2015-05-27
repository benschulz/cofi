package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.MemberSort;

public class PropertyDeclaration<X extends ModelContext<X>> extends MemberDeclarationImpl<X> implements AnnotatedNodeMixin<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> PropertyDeclaration<X> of(ImmutableList<AnnotationImpl<X>> annotations,
                                                                        ImmutableList<ModifierImpl<X>> modifiers, Token id, ImmutableList<TypeExpression<X>> traits,
                                                                        TypeExpression<X> type, ExpressionNode<X> initialValue) {
        return new PropertyDeclaration<>(annotations, modifiers, id, traits, type, initialValue);
    }

    public final ImmutableList<TypeExpression<X>> traits;
    public final Token id;
    public final TypeExpression<X> type;
    public final ExpressionNode<X> initialValue;

    public TraitDeclaration<X> traitDeclaration;

    private PropertyDeclaration(ImmutableList<AnnotationImpl<X>> annotations,
                                ImmutableList<ModifierImpl<X>> modifiers, Token id, ImmutableList<TypeExpression<X>> traits,
                                TypeExpression<X> type, ExpressionNode<X> initialValue) {
        super(annotations, modifiers);

        this.traits = traits;
        this.type = type;
        this.id = id;
        this.initialValue = initialValue == null ? null : initialValue;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitPropertyDeclaration(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> L accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformPropertyDeclaration(this);
    }

    @Override
    public MemberSort getSort() {
        return MemberSort.PROPERTY;
    }

    @Override
    public String getName() {
        return id.getLexeme();
    }

    @Override
    boolean isMember() {
        return true;
    }

    public void setTraitDeclaration(TraitDeclaration<X> td) {
        traitDeclaration = td;
    }
}
