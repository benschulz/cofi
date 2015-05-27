package de.benshu.cofi.parser.lexer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

import static java.util.stream.Collectors.joining;

public class DefaultTokenString extends AbstractTokenString {
    public static DefaultTokenString create(final ImmutableList<Token> tokens) {
        return new DefaultTokenString(tokens);
    }

    private final ImmutableList<Token> tokens;

    private DefaultTokenString(final ImmutableList<Token> tokens) {
        if (tokens == null || tokens.isEmpty() || !isValidString(tokens)) {
            throw new IllegalArgumentException(String.valueOf(tokens));
        }

        this.tokens = tokens;
    }

    @Override
    public int getBeginColumn() {
        return getFirst().getBeginColumn();
    }

    @Override
    public int getBeginLine() {
        return getFirst().getBeginLine();
    }

    @Override
    public int getEndColumn() {
        return getLast().getEndColumn();
    }

    @Override
    public int getEndLine() {
        return getLast().getEndLine();
    }

    @Override
    public Token getFirst() {
        return getTokens().get(0);
    }

    @Override
    public Token getLast() {
        return getTokens().get(getLength() - 1);
    }

    @Override
    public String getLexeme() {
        final List<String> lexemes = Lists.transform(getTokens(), new Function<Token, String>() {
            @Override
            public String apply(Token t) {
                return t.getLexeme();
            }
        });

        return getTokens().stream().map(Token::getLexeme).collect(joining());
    }

    @Override
    public ImmutableList<Token> getTokens() {
        return tokens;
    }

    /**
     * @param string token string to check
     * @return whether string is a valid token string, i.e. whether one starts where the previous
     * ended.
     */
    private boolean isValidString(ImmutableList<Token> string) {
        if (string.size() >= 2) {
            final Token first = string.get(0);
            final Token second = string.get(1);

            if (second.getBeginLine() != first.getEndLine() || second.getBeginColumn() != first.getEndColumn()) {
                return false;
            }

            return isValidString(string.subList(1, string.size()));
        } else {
            return true;
        }
    }
}
