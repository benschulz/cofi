package de.benshu.cofi.parser.lexer.impl;

import de.benshu.cofi.parser.lexer.Token;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TokenImplTest {
	@Test
	public void next() {
		final LexerMock lexer = new LexerMock();
		
		lexer.addToken(Token.Kind.IDENTIFIER, "id", 1, 3);
		lexer.addToken(Token.Kind.SYMBOL, "?", 1, 4);
		lexer.addToken(Token.Kind.SYMBOL, "!", 1, 5);
		
		final TokenImpl first = (TokenImpl) lexer.getNextToken();
		
		assertEquals(Token.Kind.IDENTIFIER, first.getKind());
		
		assertEquals(Token.Kind.SYMBOL, first.next().getKind());
		assertEquals(Token.Kind.SYMBOL, first.next().getKind());
		
		assertEquals(Token.Kind.SYMBOL, first.next().next().getKind());
		assertEquals(Token.Kind.SYMBOL, first.next().next().getKind());
	}
}
