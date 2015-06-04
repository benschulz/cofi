package de.benshu.cofi.model.impl;


import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;

public class LiteralTypeExpression<X extends ModelContext<X>> extends TypeExpression<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> LiteralTypeExpression<X> of(Token literal) {
        return new LiteralTypeExpression<>(literal);
    }

    public final Token literal;

    private LiteralTypeExpression(Token literal) {
        this.literal = literal;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitLiteralType(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> T accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformLiteralType(this);
    }
}
