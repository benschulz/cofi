package de.benshu.cofi.parser.lexer;


public interface TokenStream {
    interface Scout {
        boolean scout() throws LexerException;
    }

    Token attemptConsume(Token.Kind... tokenKinds);

    /**
     * Consumes the next token if its kind is matched.
     *
     * @param tokenKinds array of kinds which the next token is expected to be of
     * @return the consumed token
     * @throws UnexpectedTokenException if the next token could not be consumed
     */
    Token consume(Token.Kind... tokenKinds) throws UnexpectedTokenException;

    /**
     * Consumes multiple tokens if they are of the correct kind.
     *
     * @param tokenKinds
     * @return the last token
     * @throws UnexpectedTokenException
     */
    TokenString consumeString(Token.Kind... tokenKinds) throws UnexpectedTokenException;

    /**
     * Checks whether the next token is of one of the specified kinds.
     *
     * @param tokenKinds the kinds to check against
     * @return <code>true</code> if and only if the next token's kind is contained in
     * {@code tokenKinds}
     */
    boolean isNext(Token.Kind... tokenKinds);

    boolean lookAhead(Scout scout);

    /**
     * Checks whether the next {@code n} tokens are of the specified kinds.
     * <p/>
     * <p>
     * Let <code>k<sub>1</sub>, ..., k<sub>n</sub></code> be the specified token kinds and
     * <code> t<sub>1</sub>, ..., t<sub>n</sub></code> be the next {@code n} tokens.
     * <code>lookAhead(k<sub>1</sub>, ..., k<sub>n</sub>)</code> will return {@code true} if and only
     * if <code>k<sub>i</sub>.matchingKinds().contains(t<sub>i</sub>.getSort())</code> is true for all
     * {@code 1 ≤ i ≤ n}.
     * </p>
     *
     * @param tokenKinds
     * @return whether the next {@code n} tokens are of the specified kinds.
     */
    boolean lookAhead(Token.Kind... tokenKinds);

    /**
     * If the next token in the stream is syntactically unexpected, this method may be called to throw
     * the appropriate exception.
     * <p/>
     * <p>
     * This method does <strong>not</strong> return an {@code UnexpectedTokenException} it throws it.
     * The return type allows writing
     * </p>
     * <p/>
     * <p>
     * {@code throw tokenStream.throwUnexpectedTokenException();}
     * </p>
     * <p/>
     * <p>
     * so that the compiler can do control flow analysis.
     * </p>
     *
     * @return nothing
     * @throws UnexpectedTokenException always
     */
    UnexpectedTokenException throwUnexpectedTokenException() throws UnexpectedTokenException;
}