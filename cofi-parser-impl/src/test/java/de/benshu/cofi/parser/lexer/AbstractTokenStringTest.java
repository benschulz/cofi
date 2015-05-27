package de.benshu.cofi.parser.lexer;

import de.benshu.cofi.parser.lexer.impl.TokenMock;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class AbstractTokenStringTest {
	private static TokenStringMock make() {
		final TokenMock t5 = new TokenMock(Token.Kind.SYMBOL, 2, 5, "!", 2, 6);
		final TokenMock t4 = new TokenMock(Token.Kind.IDENTIFIER, 2, 1, "dude", 2, 5, t5);
		final TokenMock t3 = new TokenMock(Token.Kind.WHITESPACE, 1, 7, "\n", 2, 1, t4);
		final TokenMock t2 = new TokenMock(Token.Kind.SYMBOL, 1, 6, ",", 1, 7, t3);
		final TokenMock t1 = new TokenMock(Token.Kind.IDENTIFIER, 1, 1, "Hello", 1, 6, t2);
		
		return new TokenStringMock(t1, t2, t3, t4, t5);
	}
	
	@Test
	public void getLength() {
		assertEquals(5, make().getLength());
	}
	
	@Test
	public void substring() {
		TokenString ss = make().substring(1, 4);
		
		assertEquals(3, ss.getLength());
		assertEquals(",\ndude", ss.getLexeme());
		assertEquals(1, ss.getBeginLine());
		assertEquals(6, ss.getBeginColumn());
		assertEquals(2, ss.getEndLine());
		assertEquals(5, ss.getEndColumn());
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void substringFromGreaterEqualTo() {
		make().substring(3, 3);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void substringFromNegative() {
		make().substring(-1, 3);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void substringToGreaterLength() {
		make().substring(3, 6);
	}
}
