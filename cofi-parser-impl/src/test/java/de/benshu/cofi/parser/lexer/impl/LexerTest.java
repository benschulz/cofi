package de.benshu.cofi.parser.lexer.impl;

import de.benshu.cofi.parser.lexer.Token;
import de.benshu.commons.core.exception.UnexpectedCheckedException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.testng.Assert.*;

public class LexerTest {
	private static final class LimitedReader extends Reader {
		private final StringReader reader;
		
		// will fail after returning data
		public LimitedReader(String data) {
			reader = new StringReader(data);
		}
		
		@Override
		public void close() throws IOException {}
		
		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			final int read = reader.read(cbuf, off, len);
			
			if (read < 0) {
				throw new IOException("I fail!");
			}
			
			return read;
		}
		
	}
	
	private static Token token(String token) {
		return token(token, ":tailing");
	}
	
	private static Token token(String token, String trailing) {
		return Lexer.getFirstToken(new StringReader(token + trailing));
	}
	
	@Test(expectedExceptions = UnexpectedCheckedException.class)
	public void bailOnCreateMockSource() {
		Lexer.createMockSource(new LimitedReader(""));
	}
	
	@Test
	public void characterLiteralEndingInEscapedEOF() {
		final String lexeme = "'\\";
		final Token token = token(lexeme, "");
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.CHARACTER_LITERAL, token.getKind());
	}
	
	@Test
	public void characterLiteralEndingInEscapedNewLine() {
		final String lexeme = "'\\\n";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.CHARACTER_LITERAL, token.getKind());
	}
	
	@Test
	public void empty() {
		assertEquals(Token.Kind.EOF, token("", "").getKind());
	}
	
	@Test
	public void emptyCharacterLiteral() {
		final String lexeme = "''";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.CHARACTER_LITERAL, token.getKind());
	}
	
	@Test
	public void emptyStringLiteral() {
		final String lexeme = "\"\"";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.STRING_LITERAL, token.getKind());
	}
	
	@Test
	public void eofTerminatedCharacterLiteral() {
		final String lexeme = "'a";
		final Token token = token(lexeme, "");
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.CHARACTER_LITERAL, token.getKind());
	}
	
	@Test
	public void eofTerminatedIdentifier() {
		final String lexeme = "_thisIsAn_1d3nt1f132";
		final Token token = token(lexeme, "");
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.IDENTIFIER, token.getKind());
	}
	
	@Test
	public void eofTerminatedIntegerLiteral() {
		final String lexeme = "1337";
		final Token token = token(lexeme, "");
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.NUMERICAL_LITERAL, token.getKind());
	}
	
	@Test
	public void eofTerminatedStringLiteral() {
		final String lexeme = "\"a";
		final Token token = token(lexeme, "");
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.STRING_LITERAL, token.getKind());
	}
	
	@Test
	public void escapedCharacterLiteral() {
		final String lexeme = "'\\''";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.CHARACTER_LITERAL, token.getKind());
	}
	
	@Test
	public void escapedStringLiteral() {
		final String lexeme = "\"\\\"still the same string literal\"";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.STRING_LITERAL, token.getKind());
	}
	
	@Test
	public void getFirstTokenWithBadReaderProducesEOF() {
		assertEquals(Token.Kind.EOF, Lexer.getFirstToken(new LimitedReader("")).getKind());
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void getFirstTokenWithNullArgument() {
		Lexer.getFirstToken(null);
	}
	
	@Test
	public void identifier() {
		final String lexeme = "_thisIsAn_1d3nt1f132";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.IDENTIFIER, token.getKind());
	}
	
	@Test
	public void ioExceptionDuringLexingProducesEOF() {
		final AbstractTokenImpl first = Lexer.getFirstToken(new LimitedReader("will fail now"));
		
		assertEquals(Token.Kind.IDENTIFIER, first.getKind());
		assertEquals(Token.Kind.WHITESPACE, first.next().getKind());
		assertEquals(Token.Kind.IDENTIFIER, first.next().next().getKind());
		assertEquals(Token.Kind.WHITESPACE, first.next().next().next().getKind());
		assertEquals(Token.Kind.EOF, first.next().next().next().next().getKind());
	}
	
	@Test
	public void newLineTerminatedCharacterLiteral() {
		final String lexeme = "'a\n";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.CHARACTER_LITERAL, token.getKind());
	}
	
	@Test
	public void newLineTerminatedStringLiteral() {
		final String lexeme = "\"\n";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.STRING_LITERAL, token.getKind());
	}
	
	@Test
	public void simpleCharacterLiteral() {
		final String lexeme = "'a'";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.CHARACTER_LITERAL, token.getKind());
	}
	
	@Test
	public void simpleFractionLiteral() {
		final String lexeme = "1.5";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.NUMERICAL_LITERAL, token.getKind());
	}
	
	@Test
	public void simpleIntegerLiteral() {
		final String lexeme = "42";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.NUMERICAL_LITERAL, token.getKind());
	}
	
	@Test
	public void simpleStringLiteral() {
		final String lexeme = "\"string\"";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.STRING_LITERAL, token.getKind());
	}
	
	@Test
	public void stringLiteralEndingInEscapedEOF() {
		final String lexeme = "\"\\";
		final Token token = token(lexeme, "");
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.STRING_LITERAL, token.getKind());
	}
	
	@Test
	public void stringLiteralEndingInEscapedNewLine() {
		final String lexeme = "\"\\\n";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.STRING_LITERAL, token.getKind());
	}
	
	@Test
	public void stringLiteralWithIllegalEscapeSeq() {
		final String lexeme = "\"\\illegal\"";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.STRING_LITERAL, token.getKind());
	}
	
	@Test
	public void tooLongCharacterLiteral() {
		final String lexeme = "'abc'";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.CHARACTER_LITERAL, token.getKind());
	}
	
	@Test
	public void whitespace() {
		final String lexeme = " ";
		final Token token = token(lexeme);
		
		assertEquals(lexeme, token.getLexeme());
		assertEquals(Token.Kind.WHITESPACE, token.getKind());
	}
}
