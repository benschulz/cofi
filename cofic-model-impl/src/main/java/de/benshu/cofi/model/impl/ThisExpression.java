package de.benshu.cofi.model.impl;

import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;

public class ThisExpression<X extends ModelContext<X>> extends ExpressionNode<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> ThisExpression<X> of(Token token) {
        return new ThisExpression<>(token);
    }

    public final Token token;

    private ThisExpression(Token token) {
        this.token = token;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitThisExpr(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> E accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformThisExpr(this);
    }

    @Override
    public String toString() {
        return "this";
    }
}
