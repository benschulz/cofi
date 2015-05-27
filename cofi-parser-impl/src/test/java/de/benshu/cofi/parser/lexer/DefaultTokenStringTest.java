package de.benshu.cofi.parser.lexer;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.lexer.impl.TokenMock;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class DefaultTokenStringTest {
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void constructorInvalidTokenStringJumpingColumn() {
		final Token token1 = new TokenMock(Token.Kind.IDENTIFIER, 1, 1, "a", 1, 2);
		final Token token2 = new TokenMock(Token.Kind.IDENTIFIER, 1, 3, "b", 1, 4);
		final ImmutableList<Token> tokens = ImmutableList.of(token1, token2);
		
		// token2 does not begin where token1 ended => should fail
		DefaultTokenString.create(tokens);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void constructorInvalidTokenStringJumpingLine() {
		final Token token1 = new TokenMock(Token.Kind.IDENTIFIER, 1, 1, "a", 1, 2);
		final Token token2 = new TokenMock(Token.Kind.IDENTIFIER, 2, 1, "b", 2, 2);
		final ImmutableList<Token> tokens = ImmutableList.of(token1, token2);
		
		// token2 does not begin where token1 ended => should fail
		DefaultTokenString.create(tokens);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void constructorNoElements() {
		final ImmutableList<Token> tokens = ImmutableList.of();
		DefaultTokenString.create(tokens);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void constructorNullArg() {
		DefaultTokenString.create(null);
	}
	
	@Test
	public void constructorOneElement() {
		final Token token = new TokenMock(Token.Kind.SYMBOL, 1, 1, "*", 1, 2);
		final ImmutableList<Token> tokens = ImmutableList.of(token);
		
		final DefaultTokenString ts = DefaultTokenString.create(tokens);
		
		assertEquals(1, ts.getBeginLine());
		assertEquals(1, ts.getBeginColumn());
		assertEquals("*", ts.getLexeme());
		assertEquals(1, ts.getEndLine());
		assertEquals(2, ts.getEndColumn());
		assertEquals(1, ts.getLength());
	}
	
	@Test
	public void constructorThreeElements() {
		final Token token1 = new TokenMock(Token.Kind.IDENTIFIER, 1, 1, "a", 1, 2);
		final Token token2 = new TokenMock(Token.Kind.WHITESPACE, 1, 2, "\n", 2, 1);
		final Token token3 = new TokenMock(Token.Kind.IDENTIFIER, 2, 1, "b", 2, 2);
		final ImmutableList<Token> tokens = ImmutableList.of(token1, token2, token3);
		
		final DefaultTokenString ts = DefaultTokenString.create(tokens);
		
		assertEquals(1, ts.getBeginLine());
		assertEquals(1, ts.getBeginColumn());
		assertEquals("a\nb", ts.getLexeme());
		assertEquals(2, ts.getEndLine());
		assertEquals(2, ts.getEndColumn());
		assertEquals(3, ts.getLength());
	}
}
