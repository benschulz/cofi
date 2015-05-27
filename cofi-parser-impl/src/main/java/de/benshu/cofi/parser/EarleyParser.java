package de.benshu.cofi.parser;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.Chart.Entry;
import de.benshu.cofi.parser.lexer.ArtificialToken;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.parser.lexer.TokenStream;
import de.benshu.cofi.parser.lexer.UnexpectedTokenException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public enum EarleyParser {
    INSTANCE;

    public static EarleyParser getInstance() {
        return INSTANCE;
    }

    private final List<Chart.Entry> backtrace = new ArrayList<>();

    private Chart.Entry backup(Chart.Entry entry) {
        backtrace.add(0, entry);
        final int MAX_SIZE = 3;
        while (backtrace.size() > MAX_SIZE) {
            backtrace.remove(MAX_SIZE);
        }
        return entry;
    }

    private void close(Grammar grammar, ImmutableList<Chart> charts, ImmutableList<Token> tokens, Chart chart, int index) {
        scanNewLine(grammar, charts, tokens, chart, index);
        predict(grammar, charts, tokens, chart, index);
        reconstruct(grammar, charts, tokens, chart, index);
    }

    private Object constructTree(ImmutableList<Chart> charts, Rule dummyStartRule, NonTerminal start,
                                 ImmutableList<Token> tokens) {
        for (Chart.Entry entry : charts.get(charts.size() - 1)) {
            if (entry.rule.equals(dummyStartRule) && entry.isCompleted()) {
                return constructTree(start, entry.getParent(), tokens.reverse().iterator());
            }
        }

        int index = 0;
        for (; index < charts.size(); ++index) {
            if (!charts.get(index).iterator().hasNext()) {
                break;
            }
        }

        final StringBuilder expected = new StringBuilder();
        for (Chart.Entry entry : charts.get(index - 1)) {
            if (entry.rule.production.size() <= entry.dot) {
                continue;
            }

            final Symbol symbol = entry.rule.production.get(entry.dot);
            if (symbol.kind == Symbol.Kind.TERMINAL) {
                final Terminal terminal = (Terminal) symbol;
                expected.append(terminal.token == Token.Kind.SYMBOL ? "'" + terminal.lexeme + "'" : terminal.token);
                expected.append(", ");
            }
        }
        expected.setLength(expected.length() - 2);

        // TODO the output is generally useless
        Token found = tokens.get(index);
        throw new RuntimeException("Input was not accepted [" + found.getBeginLine() + ":" + found.getBeginColumn()
                + "].\n\tExpected: " + expected + "\n\tFound: " + found);
    }

    // TODO nt == entry.rule.nonTerminal ??
    private Object constructTree(NonTerminal nt, Chart.Entry entry, Iterator<Token> tokens) {
        return constructTreeInternal(nt, entry, tokens).getTree();
    }

    private Chart.Entry.Constructed constructTreeInternal(NonTerminal nt, Chart.Entry entry, Iterator<Token> tokens) {
        Chart.Entry current = entry;

        List<Object> subtrees = new ArrayList<>(current.rule.production.size());

        for (int dot = entry.rule.production.size(); dot > 0; --dot) {
            Chart.EntryRelationship rs = current.getRelationship();
            current = backup(current).getParent();

            switch (rs) {
                case INITIALIZATION:
                    throw new AssertionError();
                case PREDICTION:
                    throw new AssertionError();
                case RECONSTRUCTION:
                    final Chart.Entry.Constructed constructed = constructTreeInternal(current.rule.nonTerminal, current, tokens);
                    current = constructed.getChartEntry();
                    subtrees.add(constructed.getTree());
                    break;
                case SCANNING:
                    final int count = current.getPredicted().getTokenCount();

                    switch (count) {
                        case 0:
                            // TODO add the correct token
                            subtrees.add(ArtificialToken.create(-1, -1, Token.Kind.WHITESPACE, "\n", -1, -1));
                            break;
                        case 1:
                            subtrees.add(tokens.next());
                            break;
                        default:
                            final Token last = tokens.next();
                            for (int i = 2; i < count; ++i) {
                                tokens.next();
                            }
                            subtrees.add(tokens.next().getTokenString(last));
                            break;
                    }
                    break;
                default:
                    throw new AssertionError();
            }

            if (!current.rule.nonTerminal.equals(entry.rule.nonTerminal)
                    || !current.rule.production.equals(entry.rule.production)) {
                // we had the wrong parent (chart.add(Chart.Entry) overrides predictions)
                current = current.getChart().getEntry(current.getChart().createFinderEntry(entry, dot - 1));
            }
        }

        final Object[] args = new Object[entry.rule.argIndices.size()];
        for (int i = 0; i < args.length; ++i) {
            final int argIndex = entry.rule.argIndices.get(i);
            args[i] = argIndex <= 0 ? null : subtrees.get(subtrees.size() - argIndex);
        }

        try {
            return backup(current).getParent().construct(nt.factory.create(args));
        } catch (RuntimeException e) {
            throw new RuntimeException("Factory invocation of " + nt + " failed. Rule: " + entry.rule + " Arguments: "
                    + Arrays.toString(args), e);
        }
    }

    private ImmutableList<Chart> createCharts(int tokens) {
        final ImmutableList.Builder<Chart> builder = ImmutableList.builder();

        for (int i = 0; i <= tokens; ++i) {
            builder.add(Chart.create(i));
        }

        return builder.build();
    }

    private void earley(Grammar grammar, ImmutableList<Chart> charts, ImmutableList<Token> tokens) {
        for (int i = 0; i < charts.size(); ++i) {
            try {
                final Chart chart = charts.get(i);
                scan(grammar, charts, chart, tokens, i);
                close(grammar, charts, tokens, chart, i);
            } catch (AmbiguityException e) {
                try {
                    final ImmutableList<Token> sublist = tokens.subList(0, i);
                    Object a = constructTree(e.a.rule.nonTerminal, e.a, sublist.reverse().iterator());
                    Object b = constructTree(e.b.rule.nonTerminal, e.b, sublist.reverse().iterator());

                    System.out.println(a);
                    System.out.println(b);
                } catch (Exception e2) {
                    // can't construct the ambiguous trees :(
                    e2.printStackTrace();
                } finally {
                    throw e;
                }
            }
        }
    }

    public Object parse(Grammar grammar, TokenStream tokenStream) {
        final NonTerminal startDummy = NonTerminal.create("_S_", null);
        final Rule dummyStartRule = Rule.create(startDummy, ImmutableList.<Symbol>of(grammar.start));

        final ImmutableList<Token> tokens = readTokens(tokenStream);
        final ImmutableList<Chart> charts = createCharts(tokens.size());

        charts.get(0).add(charts.get(0).createInitEntry(dummyStartRule));

        earley(grammar, charts, tokens);

        return constructTree(charts, dummyStartRule, grammar.start, tokens);
    }

    private void predict(Grammar grammar, ImmutableList<Chart> charts, ImmutableList<Token> tokens, Chart chart, int index) {
        for (Chart.Entry entry : chart) {
            predict(grammar, charts, tokens, chart, index, entry);
        }
    }

    private void predict(Grammar grammar, ImmutableList<Chart> charts, ImmutableList<Token> tokens, Chart chart,
                         int index, Chart.Entry entry) {
        if (!entry.isCompleted()) {
            for (Rule rule : grammar.getRules(entry.getPredicted())) {
                final Chart.Entry newEntry = chart.createPredictionEntry(entry, rule, index);

                if (chart.add(newEntry)) {
                    scanNewLine(grammar, charts, tokens, chart, index, newEntry);
                    predict(grammar, charts, tokens, chart, index, newEntry);
                    reconstruct(grammar, charts, tokens, chart, index, newEntry);
                } else if (newEntry.isCompleted()) {
                    reconstruct(grammar, charts, tokens, chart, index, newEntry, entry);
                } else {
                    for (Chart.Entry e : chart) {
                        if (e.isCompleted() && entry.getPredicted().equals(e.rule.nonTerminal)
                                && e.prediction == entry.getChart().getIndex()) {
                            reconstruct(grammar, charts, tokens, chart, index, e, entry);
                        }
                    }
                }
            }
        }
    }

    private ImmutableList<Token> readTokens(TokenStream tokenStream) {
        final ImmutableList.Builder<Token> builder = ImmutableList.builder();

        try {
            while (!tokenStream.isNext(Token.Kind.EOF)) {
                builder.add(tokenStream.consume(Token.Kind.ANY));
            }
        } catch (UnexpectedTokenException e) {
            throw new AssertionError(e);
        }

        return builder.build();
    }

    private void reconstruct(Grammar grammar, ImmutableList<Chart> charts, ImmutableList<Token> tokens, Chart chart,
                             int index) {
        for (Chart.Entry entry : chart) {
            reconstruct(grammar, charts, tokens, chart, index, entry);
        }
    }

    private void reconstruct(Grammar grammar, ImmutableList<Chart> charts, ImmutableList<Token> tokens, Chart chart,
                             int index, Chart.Entry entry) {
        if (entry.isCompleted()) {
            for (Chart.Entry e : charts.get(entry.prediction)) {
                if (!e.isCompleted() && e.getPredicted().equals(entry.rule.nonTerminal)) {
                    reconstruct(grammar, charts, tokens, chart, index, entry, e);
                }
            }
        }
    }

    private void reconstruct(Grammar grammar, ImmutableList<Chart> charts, ImmutableList<Token> tokens, Chart chart,
                             int index, Chart.Entry entry, Chart.Entry e) {
        final Chart.Entry newEntry = chart.createReconstructionEntry(e, entry);

        if (chart.add(newEntry)) {
            scanNewLine(grammar, charts, tokens, chart, index, newEntry);
            reconstruct(grammar, charts, tokens, chart, index, newEntry);
            predict(grammar, charts, tokens, chart, index, newEntry);
        }
    }

    private void scan(Chart chart, Entry entry) {
        chart.add(chart.createScanningEntry(entry));
    }

    private void scan(Grammar grammar, ImmutableList<Chart> charts, Chart chart, ImmutableList<Token> tokens, int index) {
        final int limit = Math.max(0, index - grammar.scanLimit);

        for (int j = index - 1; j >= limit; --j) {
            for (Chart.Entry entry : charts.get(j)) {
                if (!entry.isCompleted() && entry.getPredicted().matches(tokens, j, index - j)) {
                    scan(chart, entry);
                }
            }
        }
    }

    private void scanNewLine(Grammar grammar, ImmutableList<Chart> charts, ImmutableList<Token> tokens, Chart chart,
                             int index) {
        for (Chart.Entry entry : chart) {
            scanNewLine(grammar, charts, tokens, chart, index, entry);
        }
    }

    private void scanNewLine(Grammar grammar, ImmutableList<Chart> charts, ImmutableList<Token> tokens, Chart chart,
                             int index, Entry entry) {
        if (!entry.isCompleted() && entry.getPredicted() == Terminal.NEW_LINE
                && tokens.get(index - 1).getEndLine() < tokens.get(index).getBeginLine()) {
            final Chart.Entry newEntry = chart.createScanningEntry(entry);

            if (chart.add(newEntry)) {
                predict(grammar, charts, tokens, chart, index, newEntry);
                reconstruct(grammar, charts, tokens, chart, index, newEntry);
            }
        }
    }
}
