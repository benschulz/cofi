package de.benshu.cofi.parser.lexer;

import com.google.common.collect.ImmutableList;

public abstract class AbstractTokenString implements TokenString {
    @Override
    public int getLength() {
        return getTokens().size();
    }

    @Override
    public TokenString substring(int from, int to) {
        final ImmutableList<Token> tokens = getTokens();

        if (from < 0 || from >= to || to > tokens.size()) {
            throw new IllegalArgumentException("to=" + to + ", from=" + from);
        }

        return DefaultTokenString.create(getTokens().subList(from, to));
    }

    @Override
    public String toString() {
        return getLexeme();
    }
}
