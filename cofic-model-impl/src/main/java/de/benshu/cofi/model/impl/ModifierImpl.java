package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.model.Modifier;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.ArtificialToken;
import de.benshu.cofi.parser.lexer.Token;

public class ModifierImpl<X extends ModelContext<X>> extends AnnotationImpl<X> implements Modifier<X> {
    public static <X extends ModelContext<X>> ImmutableList<ModifierImpl<X>> noModifiers() {
        return ImmutableList.of();
    }

    private static Kind getKindForToken(Token token) {
        switch (token.getKind()) {
            case ABSTRACT:
                return Kind.ABSTRACT;
            case EXPLICIT:
                return Kind.EXPLICIT;
            case FINAL:
                return Kind.FINAL;
            case IMPLICIT:
                return Kind.IMPLICIT;
            case MODULE:
                return Kind.MODULE;
            case PACKAGE:
                return Kind.PACKAGE;
            case PRIVATE:
                return Kind.PRIVATE;
            case PUBLIC:
                return Kind.PUBLIC;
            case SEALED:
                return Kind.SEALED;
            default:
                throw new IllegalArgumentException(token.toString());
        }
    }

    private static String getKindTypeName(Kind kind) {
        switch (kind) {
            case ABSTRACT:
                return "Abstract";
            case EXPLICIT:
                return "Explicit";
            case FINAL:
                return "Final";
            case IMPLICIT:
                return "Implicit";
            case MODULE:
                return "Module";
            case PACKAGE:
                return "Package";
            case PRIVATE:
                return "Private";
            case PUBLIC:
                return "Public";
            case SEALED:
                return "Sealed";
            default:
                throw new IllegalArgumentException(kind.toString());
        }
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> ModifierImpl<X> of(Token token) {
        if (token.isA(Token.Kind.MODIFIER)) {
            return new ModifierImpl<>(token);
        } else {
            throw new IllegalArgumentException(token.toString());
        }
    }

    public final Kind kind;

    public final Token token;

    private final NamedTypeExpression<X> type;

    public ModifierImpl(Token token) {
        super(null, ImmutableList.<PropertyAssignment<X>>of());

        final FullyQualifiedName<X> fqn = FullyQualifiedName.create(ArtificialToken.createString(token, Token.Kind.IDENTIFIER, "cofi", "lang", "modifier", getKindTypeName(getKindForToken(token))).getTokens());

        this.kind = getKindForToken(token);
        this.token = token;
        this.type = NamedTypeExpression.of2(fqn);
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitModifier(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformModifier(this);
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public NamedTypeExpression<X> getTypeExpression() {
        return type;
    }
}
