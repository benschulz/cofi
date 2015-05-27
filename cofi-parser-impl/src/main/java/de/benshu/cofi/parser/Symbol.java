package de.benshu.cofi.parser;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.lexer.Token;

public abstract class Symbol {
    enum Kind {
        NONTERMINAL,
        TERMINAL
    }

    public final Kind kind;

    Symbol(Kind kind) {
        this.kind = kind;
    }

    public abstract int getTokenCount();

    public abstract boolean matches(ImmutableList<Token> tokens, int j, int i);
}
