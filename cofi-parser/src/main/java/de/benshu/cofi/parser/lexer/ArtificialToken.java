package de.benshu.cofi.parser.lexer;

import com.google.common.collect.ImmutableList;

public class ArtificialToken extends AbstractToken {
    public static ArtificialToken dupe(Token token) {
        return create(token.getBeginLine(), token.getBeginColumn(), token.getKind(), token.getLexeme(), token.getEndLine(), token.getEndColumn());
    }

    public static ArtificialToken create(Kind kind, String lexeme) {
        return new ArtificialToken(-1, -1, kind, lexeme, -1, -1, null);
    }

    public static ArtificialToken create(int beginLine, int beginColumn, Kind kind, String lexeme, int endLine,
                                         int endColumn) {
        return new ArtificialToken(beginLine, beginColumn, kind, lexeme, endLine, endColumn, null);
    }

    public static Token create(TokenString range, Kind kind, String lexeme) {
        return create(range.getBeginLine(), range.getBeginColumn(), kind, lexeme, range.getEndLine(), range.getEndColumn());
    }

    public static TokenString createString(int beginLine, int beginColumn, Kind kind, int endLine, int endColumn,
                                           String... lexemes) {

        ArtificialToken last = null;
        for (int i = lexemes.length - 1; i > 0; --i) {
            last = new ArtificialToken(endLine, endColumn, kind, lexemes[i], endLine, endColumn, last);
        }

        final ArtificialToken first = new ArtificialToken(beginLine, beginColumn, kind, lexemes[0], endLine, endColumn,
                last);

        last = first;
        while (last.next != null) {
            last = last.next;
        }

        return first.getTokenString(last);
    }

    public static TokenString createString(TokenString range, Kind kind, String... lexemes) {
        return createString(range.getBeginLine(), range.getBeginColumn(), kind, range.getEndLine(), range.getEndColumn(),
                lexemes);
    }

    private final Kind kind;
    private final ArtificialToken next;

    private ArtificialToken(int beginLine, int beginColumn, Kind kind, String lexeme, int endLine, int endColumn,
                            ArtificialToken next) {
        super(beginLine, beginColumn, lexeme, endLine, endColumn);

        this.kind = kind;
        this.next = next;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public TokenString getTokenString(Token successor) {
        final ImmutableList.Builder<Token> builder = ImmutableList.builder();
        builder.add(this);

        ArtificialToken current = this;
        while (!current.equals(successor)) {
            current = current.next;
            builder.add(current);
        }

        return DefaultTokenString.create(builder.build());
    }
}
