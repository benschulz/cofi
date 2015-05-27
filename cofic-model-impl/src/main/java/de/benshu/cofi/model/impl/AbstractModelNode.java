package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.model.ModelNode;
import de.benshu.cofi.parser.lexer.ArtificialToken;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.parser.lexer.TokenString;

import java.util.List;

abstract class AbstractModelNode<X extends ModelContext<X>> implements ModelNode<X>, ModelNodeMixin<X> {
    public AbstractModelNode() { }

    @Override
    public boolean isSynthetic() {
        return false;
    }

    @Override // FIXME remove
    public void markSynthetic() {
        throw null;
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final String toString() {
        TokenString sourceSnippet = getSourceSnippet();
        return sourceSnippet.getLexeme() + " @ " + sourceSnippet.getFirst().getBeginLine() + ":" + sourceSnippet.getFirst().getBeginColumn();
    }

    public final TokenString getSourceSnippet() {
        final ImmutableList<Token> tokens = accept(new TraversingModelVisitor<X, ImmutableList.Builder<Token>>() {
            public ImmutableList.Builder<Token> visitToken(Token token, ImmutableList.Builder<Token> aggregate) {
                return aggregate.add(token);
            }
        }, ImmutableList.builder()).build();

        return tokens.isEmpty()
                ? ArtificialToken.create(0, 0, Token.Kind.WHITESPACE, "<empty source snippet>", 0, 0)
                : tokenString(tokens);
    }

    private TokenString tokenString(List<Token> tokens) {
        try {
            return tokens.get(0).getTokenString(tokens.get(tokens.size() - 1));
        } catch (Exception e) {
            return tokens.get(0);
        }
    }
}
