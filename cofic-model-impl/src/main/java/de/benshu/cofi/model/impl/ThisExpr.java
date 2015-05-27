package de.benshu.cofi.model.impl;

import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;

public class ThisExpr<X extends ModelContext<X>> extends ExpressionNode<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> ThisExpr<X> of(Token token) {
        return new ThisExpr<>(token);
    }

    public final Token token;

    private ThisExpr(Token token) {
        this.token = token;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitThisExpr(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> E accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformThisExpr(this);
    }
}
