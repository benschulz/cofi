package de.benshu.cofi.model.impl;

import com.google.common.base.Preconditions;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;

public class LiteralExpression<X extends ModelContext<X>> extends ExpressionNode<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> LiteralExpression<X> of(Token token) {
        return new LiteralExpression<>(token);
    }

    public final Token literal;

    private LiteralExpression(Token literal) {
        Preconditions.checkArgument(literal != null && literal.isA(Token.Kind.LITERAL));

        this.literal = literal;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitLiteralExpression(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> E accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformLiteralExpression(this);
    }

}
