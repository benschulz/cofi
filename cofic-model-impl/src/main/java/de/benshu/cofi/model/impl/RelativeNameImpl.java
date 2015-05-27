package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;

public class RelativeNameImpl<X extends ModelContext<X>> extends NameImpl<X> {
    public static <X extends ModelContext<X>> RelativeNameImpl<X> of(Token id) {
        return of(id, ImmutableList.of());
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> RelativeNameImpl<X> of(Token id, ImmutableList<TypeExpression<X>> typeArgs) {
        return new RelativeNameImpl<>(ImmutableList.of(id), typeArgs);
    }

    public RelativeNameImpl(ImmutableList<Token> ids, ImmutableList<TypeExpression<X>> typeArgs) {
        super(ids, typeArgs);
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitRelativeName(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformRelativeName(this);
    }
}
