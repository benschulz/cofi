package de.benshu.cofi.parser.lexer.impl;

import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.parser.lexer.TokenStream;
import de.benshu.cofi.parser.lexer.TokenString;
import de.benshu.cofi.parser.lexer.UnexpectedTokenException;
import org.testng.annotations.Test;

import java.io.StringReader;

import static org.testng.Assert.*;

public class TokenStreamImplTest {
	@Test
	public void attemptConsumeFailure() {
		final LexerMock l = new LexerMock();
		l.addToken(Token.Kind.IDENTIFIER, "Hello", 1, 7);
		l.addToken(Token.Kind.SYMBOL, "!", 1, 8);
		
		final TokenStreamImpl ts = new TokenStreamImpl(l.getNextToken());
		
		assertNull(ts.attemptConsume(Token.Kind.SYMBOL));
		assertNull(ts.attemptConsume(Token.Kind.SYMBOL, Token.Kind.SYMBOL));
		assertNull(ts.attemptConsume(Token.Kind.LITERAL));
	}
	
	@Test
	public void attemptConsumeSuccess() {
		final LexerMock l = new LexerMock();
		l.addToken(Token.Kind.IDENTIFIER, "Hello", 1, 6);
		l.addToken(Token.Kind.SYMBOL, ",", 1, 7);
		l.addToken(Token.Kind.IDENTIFIER, "world", 1, 12);
		l.addToken(Token.Kind.SYMBOL, "!", 1, 13);
		
		final TokenStreamImpl ts = new TokenStreamImpl(l.getNextToken());
		
		assertEquals(Token.Kind.IDENTIFIER, ts.attemptConsume(Token.Kind.IDENTIFIER).getKind());
		assertEquals(Token.Kind.SYMBOL, ts.attemptConsume(Token.Kind.SYMBOL, Token.Kind.SYMBOL).getKind());
		assertEquals(Token.Kind.IDENTIFIER, ts.attemptConsume(Token.Kind.SYMBOL, Token.Kind.IDENTIFIER, Token.Kind.SYMBOL)
		    .getKind());
		assertEquals(Token.Kind.SYMBOL, ts.attemptConsume(Token.Kind.ANY).getKind());
	}
	
	@Test(expectedExceptions = UnexpectedTokenException.class)
	public void consumeFailure() throws UnexpectedTokenException {
		final LexerMock l = new LexerMock();
		l.addToken(Token.Kind.IDENTIFIER, "Hello", 1, 7);
		
		final TokenStreamImpl ts = new TokenStreamImpl(l.getNextToken());
		
		ts.consume(Token.Kind.SYMBOL);
	}
	
	@Test(expectedExceptions = UnexpectedTokenException.class)
	public void consumeStringFailure() throws UnexpectedTokenException {
		final LexerMock l = new LexerMock();
		l.addToken(Token.Kind.IDENTIFIER, "Hello", 1, 6);
		l.addToken(Token.Kind.SYMBOL, ",", 1, 7);
		l.addToken(Token.Kind.IDENTIFIER, "world", 1, 12);
		l.addToken(Token.Kind.SYMBOL, "!", 1, 13);
		
		final TokenStreamImpl ts = new TokenStreamImpl(l.getNextToken());
		
		ts.consumeString(Token.Kind.IDENTIFIER, Token.Kind.STRING_LITERAL, Token.Kind.IDENTIFIER);
	}
	
	@Test
	public void consumeStringSuccess() throws UnexpectedTokenException {
		final LexerMock l = new LexerMock();
		l.addToken(Token.Kind.IDENTIFIER, "Hello", 1, 6);
		l.addToken(Token.Kind.SYMBOL, ",", 1, 7);
		l.addToken(Token.Kind.IDENTIFIER, "world", 1, 12);
		l.addToken(Token.Kind.SYMBOL, "!", 1, 13);
		
		final TokenStreamImpl ts = new TokenStreamImpl(l.getNextToken());
		
		assertEquals("Hello,world", ts.consumeString(Token.Kind.IDENTIFIER, Token.Kind.SYMBOL, Token.Kind.IDENTIFIER)
		    .getLexeme());
		assertEquals("!", ts.consumeString(Token.Kind.SYMBOL).getLexeme());
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void consumeStringWithEmptyArgument() throws UnexpectedTokenException {
		final TokenStreamImpl ts = new TokenStreamImpl(new EOFToken(1, 1));
		ts.consumeString();
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void consumeStringWithNullArgument() throws UnexpectedTokenException {
		final TokenStreamImpl ts = new TokenStreamImpl(new EOFToken(1, 1));
		ts.consumeString((Token.Kind[]) null);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void consumeStringWithNullElementArgument() throws UnexpectedTokenException {
		final TokenStreamImpl ts = new TokenStreamImpl(new EOFToken(1, 1));
		ts.consumeString((Token.Kind) null);
	}
	
	@Test
	public void consumeSuccess() throws UnexpectedTokenException {
		final LexerMock l = new LexerMock();
		l.addToken(Token.Kind.IDENTIFIER, "Hello", 1, 6);
		l.addToken(Token.Kind.SYMBOL, ",", 1, 7);
		l.addToken(Token.Kind.IDENTIFIER, "world", 1, 12);
		l.addToken(Token.Kind.SYMBOL, "!", 1, 13);
		
		final TokenStreamImpl ts = new TokenStreamImpl(l.getNextToken());
		
		assertEquals(Token.Kind.IDENTIFIER, ts.consume(Token.Kind.IDENTIFIER).getKind());
		assertEquals(Token.Kind.SYMBOL, ts.consume(Token.Kind.SYMBOL, Token.Kind.SYMBOL).getKind());
		assertEquals(Token.Kind.IDENTIFIER, ts.consume(Token.Kind.SYMBOL, Token.Kind.IDENTIFIER, Token.Kind.SYMBOL)
		    .getKind());
		assertEquals(Token.Kind.SYMBOL, ts.consume(Token.Kind.ANY).getKind());
	}
	
	@Test
	public void create() {
		final TokenStreamImpl ts = TokenStreamImpl.create(new StringReader("Hello, World!"));
		assertTrue(ts.lookAhead(Token.Kind.IDENTIFIER, Token.Kind.SYMBOL, Token.Kind.IDENTIFIER, Token.Kind.SYMBOL,
		    Token.Kind.EOF));
	}
	
	@Test
	public void isNextFalse() {
		final LexerMock l = new LexerMock();
		l.addToken(Token.Kind.IDENTIFIER, "false", 1, 6);
		l.addToken(Token.Kind.SYMBOL, "!", 1, 7);
		
		final TokenStreamImpl ts = new TokenStreamImpl(l.getNextToken());
		
		assertFalse(ts.isNext(Token.Kind.SYMBOL));
		assertFalse(ts.isNext(Token.Kind.SYMBOL, Token.Kind.STRING_LITERAL));
		assertFalse(ts.isNext(Token.Kind.SYMBOL, Token.Kind.EOF, Token.Kind.LITERAL));
		assertFalse(ts.isNext(Token.Kind.LITERAL));
	}
	
	@Test
	public void isNextFalseAfterEOF() throws UnexpectedTokenException {
		final TokenStreamImpl ts = new TokenStreamImpl(new EOFToken(1, 1));
		
		ts.consume(Token.Kind.EOF);
		
		assertFalse(ts.isNext(Token.Kind.SYMBOL));
		assertFalse(ts.isNext(Token.Kind.ANY));
	}
	
	@Test
	public void isNextTrue() {
		final LexerMock l = new LexerMock();
		l.addToken(Token.Kind.IDENTIFIER, "Hello", 1, 7);
		l.addToken(Token.Kind.SYMBOL, "!", 1, 8);
		
		final TokenStreamImpl ts = new TokenStreamImpl(l.getNextToken());
		
		assertTrue(ts.isNext(Token.Kind.IDENTIFIER));
		assertTrue(ts.isNext(Token.Kind.SYMBOL, Token.Kind.IDENTIFIER));
		assertTrue(ts.isNext(Token.Kind.SYMBOL, Token.Kind.IDENTIFIER, Token.Kind.SYMBOL));
		assertTrue(ts.isNext(Token.Kind.ANY));
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void isNextWithEmptyArgument() {
		final TokenStreamImpl ts = new TokenStreamImpl(new EOFToken(1, 1));
		ts.isNext();
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void isNextWithNullArgument() {
		final TokenStreamImpl ts = new TokenStreamImpl(new EOFToken(1, 1));
		ts.isNext((Token.Kind[]) null);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void isNextWithNullElementArgument() {
		final TokenStreamImpl ts = new TokenStreamImpl(new EOFToken(1, 1));
		ts.isNext(Token.Kind.SYMBOL, null);
	}
	
	@Test
	public void lookAheadFalse() {
		final LexerMock l = new LexerMock();
		l.addToken(Token.Kind.IDENTIFIER, "Hello", 1, 6);
		l.addToken(Token.Kind.SYMBOL, ",", 1, 7);
		l.addToken(Token.Kind.IDENTIFIER, "world", 1, 12);
		l.addToken(Token.Kind.SYMBOL, "!", 1, 13);
		
		final TokenStreamImpl ts = new TokenStreamImpl(l.getNextToken());
		
		assertFalse(ts.lookAhead(Token.Kind.SYMBOL));
		assertFalse(ts.lookAhead(Token.Kind.IDENTIFIER, Token.Kind.SYMBOL, Token.Kind.SYMBOL));
		assertFalse(ts.lookAhead(Token.Kind.IDENTIFIER, Token.Kind.SYMBOL, Token.Kind.LITERAL));
		assertFalse(ts.lookAhead(Token.Kind.IDENTIFIER, Token.Kind.LITERAL, Token.Kind.IDENTIFIER, Token.Kind.SYMBOL));
	}
	
	@Test
	public void lookAheadPastEOF() {
		final TokenStreamImpl ts = new TokenStreamImpl(new EOFToken(1, 1));
		
		assertTrue(ts.lookAhead(Token.Kind.EOF));
		
		assertFalse(ts.lookAhead(Token.Kind.EOF, Token.Kind.SYMBOL));
		assertFalse(ts.lookAhead(Token.Kind.EOF, Token.Kind.ANY));
	}
	
	@Test
	public void lookAheadTrue() {
		final LexerMock l = new LexerMock();
		l.addToken(Token.Kind.IDENTIFIER, "Hello", 1, 6);
		l.addToken(Token.Kind.SYMBOL, ",", 1, 7);
		l.addToken(Token.Kind.IDENTIFIER, "world", 1, 12);
		l.addToken(Token.Kind.SYMBOL, "!", 1, 13);
		
		final TokenStreamImpl ts = new TokenStreamImpl(l.getNextToken());
		
		assertTrue(ts.lookAhead(Token.Kind.IDENTIFIER, Token.Kind.SYMBOL, Token.Kind.IDENTIFIER, Token.Kind.SYMBOL));
		assertTrue(ts.lookAhead(Token.Kind.ANY, Token.Kind.SYMBOL, Token.Kind.IDENTIFIER, Token.Kind.SYMBOL));
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void lookAheadWithNullScout() {
		final TokenStreamImpl ts = new TokenStreamImpl(new EOFToken(1, 1));
		ts.lookAhead((TokenStream.Scout) null);
	}
	
	@Test
	public void lookAheadWithScoutFalse() {
		final LexerMock l = new LexerMock();
		l.addToken(Token.Kind.IDENTIFIER, "Hello", 1, 6);
		l.addToken(Token.Kind.SYMBOL, ",", 1, 7);
		l.addToken(Token.Kind.IDENTIFIER, "world", 1, 12);
		l.addToken(Token.Kind.SYMBOL, "!", 1, 13);
		
		final TokenStreamImpl ts = new TokenStreamImpl(l.getNextToken());
		
		assertFalse(ts.lookAhead(() -> {
            ts.consumeString(Token.Kind.IDENTIFIER, Token.Kind.SYMBOL, Token.Kind.IDENTIFIER, Token.Kind.LITERAL);
            return true;
        }));
		
		assertTrue(ts.lookAhead(Token.Kind.IDENTIFIER, Token.Kind.SYMBOL, Token.Kind.IDENTIFIER, Token.Kind.SYMBOL));
	}
	
	@Test
	public void lookAheadWithScoutTrue() {
		final LexerMock l = new LexerMock();
		l.addToken(Token.Kind.IDENTIFIER, "Hello", 1, 6);
		l.addToken(Token.Kind.SYMBOL, ",", 1, 7);
		l.addToken(Token.Kind.IDENTIFIER, "world", 1, 12);
		l.addToken(Token.Kind.SYMBOL, "!", 1, 13);
		
		final TokenStreamImpl ts = new TokenStreamImpl(l.getNextToken());
		
		assertTrue(ts.lookAhead(() -> {
            ts.consumeString(Token.Kind.IDENTIFIER, Token.Kind.SYMBOL, Token.Kind.IDENTIFIER, Token.Kind.SYMBOL);
            return true;
        }));
		
		assertTrue(ts.lookAhead(Token.Kind.IDENTIFIER, Token.Kind.SYMBOL, Token.Kind.IDENTIFIER, Token.Kind.SYMBOL));
	}
	
	@Test
	public void skipSkippables() throws UnexpectedTokenException {
		final LexerMock l = new LexerMock();
		l.addToken(Token.Kind.IDENTIFIER, "Hello", 1, 6);
		l.addToken(Token.Kind.SYMBOL, ",", 1, 7);
		l.addToken(Token.Kind.WHITESPACE, " ", 1, 8);
		l.addToken(Token.Kind.IDENTIFIER, "world", 1, 13);
		l.addToken(Token.Kind.SYMBOL, "!", 1, 14);
		
		final TokenStreamImpl ts = new TokenStreamImpl(l.getNextToken());
		
		assertTrue(ts.lookAhead(Token.Kind.IDENTIFIER, Token.Kind.SYMBOL, Token.Kind.IDENTIFIER, Token.Kind.SYMBOL));
		
		final TokenString s = ts.consumeString(Token.Kind.IDENTIFIER, Token.Kind.SYMBOL, Token.Kind.IDENTIFIER,
		    Token.Kind.SYMBOL);
		assertEquals("Hello, world!", s.getLexeme());
		assertEquals(Token.Kind.WHITESPACE, s.getTokens().get(2).getKind());
	}
	
	@Test(expectedExceptions = UnexpectedTokenException.class)
	public void throwUnexpectedTokenException() throws UnexpectedTokenException {
		final TokenStreamImpl ts = new TokenStreamImpl(new EOFToken(1, 1));
		ts.throwUnexpectedTokenException();
	}
}
