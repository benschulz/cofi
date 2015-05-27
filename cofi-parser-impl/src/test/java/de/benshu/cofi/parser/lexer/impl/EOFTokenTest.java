package de.benshu.cofi.parser.lexer.impl;

import de.benshu.cofi.parser.lexer.Token;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class EOFTokenTest {
	private final EOFToken eof = new EOFToken(5, 10);
	
	@Test
	public void beginColumn() {
		assertEquals(10, eof.getBeginColumn());
	}
	
	@Test
	public void beginLine() {
		assertEquals(5, eof.getBeginLine());
	}
	
	@Test
	public void endColumn() {
		assertEquals(10, eof.getEndColumn());
	}
	
	@Test
	public void endLine() {
		assertEquals(5, eof.getEndLine());
	}
	
	@Test
	public void getTokenString() {
		assertSame(eof, eof.getTokenString(eof));
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void getTokenStringWithBadSuccessor() {
		eof.getTokenString(new TokenMock(Token.Kind.SYMBOL, 1, 1, "!", 1, 2));
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void getTokenStringWithNullSuccessor() {
		eof.getTokenString(null);
	}
	
	@Test
	public void kind() {
		assertSame(Token.Kind.EOF, eof.getKind());
	}
	
	@Test
	public void lengthOne() {
		assertEquals(1, eof.getLength());
	}
	
	@Test
	public void lexeme() {
		assertEquals("", eof.getLexeme());
	}
	
	@Test
	public void nextNull() {
		assertNull(eof.next());
	}
	
	@Test
	public void toStringable() {
		eof.toString();
	}
}
