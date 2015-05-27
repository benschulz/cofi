package de.benshu.cofi.model.impl;

import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.impl.AbstractTypeConstructor;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.Variance;

public class TypeParamDecl<X extends ModelContext<X>> extends AbstractModelNode<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> TypeParamDecl<X> of(Token name) {
        return new TypeParamDecl<>(name, Variance.INVARIANT);
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> TypeParamDecl<X> of(Token name, Token annotation) {
        if (annotation.getKind() == Token.Kind.SYMBOL) {
            if (annotation.getLexeme().equals("+")) {
                return new TypeParamDecl<>(name, Variance.COVARIANT);
            } else if (annotation.getLexeme().equals("-")) {
                return new TypeParamDecl<>(name, Variance.CONTRAVARIANT);
            }
        } else if (annotation.getKind() == Token.Kind.IMPLICIT) {
            return new TypeParamDecl<>(name, true);
        }

        throw new IllegalArgumentException(annotation.getLexeme());
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> TypeParamDecl<X> of(Token name, Token plus, Token minus) {
        if (!plus.getLexeme().equals("+")) {
            throw new IllegalArgumentException(plus.getLexeme());
        } else if (!minus.getLexeme().equals("-")) {
            throw new IllegalArgumentException(minus.getLexeme());
        }

        return new TypeParamDecl<>(name, Variance.BIVARIANT);
    }

    public final Token name;
    public final boolean implicit;
    public final Variance variance;
    private AbstractTypeConstructor<X, ?, ? extends ProperTypeMixin<X, ?>> type;

    public TypeParamDecl(Token name, boolean implicit) {
        this.name = name;
        this.implicit = implicit;
        this.variance = Variance.INVARIANT;
    }

    public TypeParamDecl(Token name, Variance variance) {
        this.name = name;
        this.implicit = false;
        this.variance = variance;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitTypeParameterDeclaration(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformTypeParameterDeclaration(this);
    }

}
