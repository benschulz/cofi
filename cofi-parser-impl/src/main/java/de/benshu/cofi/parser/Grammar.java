package de.benshu.cofi.parser;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.parser.lexer.TokenStream;

import java.util.Collection;
import java.util.Map;

public class Grammar {
    public static Grammar create(ImmutableSet<Rule> rules, NonTerminal start) {
        return new Grammar(rules, start);
    }

    public final ImmutableSet<Rule> rules;
    public final NonTerminal start;
    final int scanLimit;

    Grammar(ImmutableSet<Rule> rules, NonTerminal start) {
        this.rules = rules;
        this.start = start;
        this.scanLimit = init();
    }

    public ImmutableSet<Rule> getRules(Symbol symbol) {
        if (symbol.kind == Symbol.Kind.TERMINAL) {
            return ImmutableSet.of();
        }

        return ((NonTerminal) symbol).getRules();
    }

    private int init() {
        int scanLimit = 1;

        final Multimap<NonTerminal, Rule> nt2r = HashMultimap.create();

        for (Rule rule : rules) {
            nt2r.put(rule.nonTerminal, rule);
        }

        for (Map.Entry<NonTerminal, Collection<Rule>> entry : nt2r.asMap().entrySet()) {
            entry.getKey().setRules(ImmutableSet.copyOf(entry.getValue()));

            for (Rule rule : entry.getValue()) {
                for (Symbol symbol : rule.production) {
                    if (symbol.kind == Symbol.Kind.NONTERMINAL) {
                        if (!nt2r.containsKey(symbol)) {
                            throw new IllegalArgumentException("No productions for " + symbol);
                        }
                    } else {
                        Terminal terminal = (Terminal) symbol;
                        if (terminal.token == Token.Kind.SYMBOL && terminal.lexeme.length() > scanLimit) {
                            scanLimit = terminal.lexeme.length();
                        }
                    }
                }
            }
        }

        return scanLimit;
    }

    public Object parse(TokenStream tokenStream) {
        return EarleyParser.getInstance().parse(this, tokenStream);
    }
}
