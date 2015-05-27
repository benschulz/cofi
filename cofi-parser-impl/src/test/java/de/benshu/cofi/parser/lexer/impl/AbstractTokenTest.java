package de.benshu.cofi.parser.lexer.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.lexer.Token;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class AbstractTokenTest {
	private static TokenMock make() {
		return make(Token.Kind.IDENTIFIER, 11, 13, "prime", 17, 19);
	}
	
	private static TokenMock make(Token.Kind kind, int beginLine, int beginColumn, String lexeme, int endLine,
	    int endColumn) {
		return make(kind, beginLine, beginColumn, lexeme, endLine, endColumn, new EOFToken(endLine, endColumn));
	}
	
	private static TokenMock make(Token.Kind kind, int beginLine, int beginColumn, String lexeme, int endLine,
	    int endColumn, AbstractTokenImpl next) {
		return new TokenMock(kind, beginLine, beginColumn, lexeme, endLine, endColumn, next);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void constructWithNullLexeme() {
		make(Token.Kind.SYMBOL, 1, 1, null, 1, 2);
	}
	
	@Test
	public void getBeginColumn() {
		assertEquals(13, make().getBeginColumn());
	}
	
	@Test
	public void getBeginLine() {
		assertEquals(11, make().getBeginLine());
	}
	
	@Test
	public void getEndColumn() {
		assertEquals(19, make().getEndColumn());
	}
	
	@Test
	public void getEndLine() {
		assertEquals(17, make().getEndLine());
	}
	
	@Test
	public void getLexeme() {
		assertEquals("prime", make().getLexeme());
	}
	
	@Test
	public void getTokens() {
		final TokenMock t = make();
		assertEquals(ImmutableList.of(t), t.getTokens());
	}
	
	@Test
	public void getTokenString() {
		final TokenMock t5 = make(Token.Kind.SYMBOL, 2, 5, "!", 2, 6);
		final TokenMock t4 = make(Token.Kind.IDENTIFIER, 2, 1, "dude", 2, 5, t5);
		final TokenMock t3 = make(Token.Kind.WHITESPACE, 1, 7, "\n", 2, 1, t4);
		final TokenMock t2 = make(Token.Kind.SYMBOL, 1, 6, ",", 1, 7, t3);
		final TokenMock t1 = make(Token.Kind.IDENTIFIER, 1, 1, "Hello", 1, 6, t2);
		
		assertEquals(3, t2.getTokenString(t4).getLength());
		assertEquals(",\ndude", t2.getTokenString(t4).getLexeme());
		
		assertEquals(5, t1.getTokenString(t5).getLength());
		assertEquals("Hello,\ndude!", t1.getTokenString(t5).getLexeme());
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void getTokenStringBadSuccessor() {
		final TokenMock t2 = make(Token.Kind.SYMBOL, 1, 6, "!", 1, 7);
		final TokenMock t1 = make(Token.Kind.IDENTIFIER, 1, 1, "Hello", 1, 6, t2);
		
		t2.getTokenString(t1);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void getTokenStringNullSuccessor() {
		final TokenMock t2 = make(Token.Kind.SYMBOL, 1, 6, "!", 1, 7);
		final TokenMock t1 = make(Token.Kind.IDENTIFIER, 1, 1, "Hello", 1, 6, t2);
		
		t1.getTokenString(null);
	}
	
	@Test
	public void isSimpleFailure() {
		assertFalse(make().isA(Token.Kind.NUMERICAL_LITERAL));
	}
	
	@Test
	public void isSimpleSuccess() {
		assertTrue(make().isA(Token.Kind.IDENTIFIER));
	}
	
	@Test
	public void isWithGroupFailure() {
		assertFalse(make().isA(Token.Kind.LITERAL));
	}
	
	@Test
	public void isWithGroupSuccess() {
		assertTrue(make().isA(Token.Kind.ANY));
	}
	
}
