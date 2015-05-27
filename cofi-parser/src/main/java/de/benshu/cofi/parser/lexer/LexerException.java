package de.benshu.cofi.parser.lexer;

public class LexerException extends RuntimeException {
    protected LexerException() {
    }

    protected LexerException(String message) {
        super(message);
    }

    protected LexerException(String message, Throwable cause) {
        super(message, cause);
    }

    protected LexerException(Throwable cause) {
        super(cause);
    }
}
