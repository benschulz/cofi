package de.benshu.cofi.parser.lexer.impl;

import de.benshu.cofi.parser.lexer.Token;
import de.benshu.commons.core.exception.UnexpectedCheckedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

class Lexer {
	private static final Logger logger = LoggerFactory.getLogger(Lexer.class);
	
	private static Map<String, Token.Kind> reservedWords = new HashMap<>();
	
	static {
		reservedWords.put("abstract", Token.Kind.ABSTRACT);
		reservedWords.put("annotation", Token.Kind.ANNOTATION);
		reservedWords.put("callback", Token.Kind.CALLBACK);
		reservedWords.put("class", Token.Kind.CLASS);
		reservedWords.put("enum", Token.Kind.ENUM);
		reservedWords.put("explicit", Token.Kind.EXPLICIT);
		reservedWords.put("extendable", Token.Kind.EXTENDABLE);
		reservedWords.put("false", Token.Kind.FALSE);
		reservedWords.put("final", Token.Kind.FINAL);
		reservedWords.put("implicit", Token.Kind.IMPLICIT);
		reservedWords.put("intersection", Token.Kind.INTERSECTION);
		reservedWords.put("module", Token.Kind.MODULE);
		reservedWords.put("nil", Token.Kind.NIL);
		reservedWords.put("object", Token.Kind.OBJECT);
		reservedWords.put("overridable", Token.Kind.OVERRIDABLE);
		reservedWords.put("package", Token.Kind.PACKAGE);
		reservedWords.put("private", Token.Kind.PRIVATE);
		reservedWords.put("public", Token.Kind.PUBLIC);
		reservedWords.put("sealed", Token.Kind.SEALED);
		reservedWords.put("super", Token.Kind.SUPER);
		reservedWords.put("this", Token.Kind.THIS);
		reservedWords.put("trait", Token.Kind.TRAIT);
		reservedWords.put("true", Token.Kind.TRUE);
		reservedWords.put("union", Token.Kind.UNION);
	}
	
	static JavaSourceReader createMockSource(Reader mockSource) {
		try {
			return new JavaSourceReader(mockSource);
		} catch (IOException e) {
			logger.error("Failed to initialize mock JavaSourceReader.", e);
			throw new UnexpectedCheckedException(e);
		}
	}
	
	public static AbstractTokenImpl getFirstToken(Reader source) {
		return new Lexer(source).getNextToken();
	}
	
	private final JavaSourceReader reader;
	private final StringBuilder image = new StringBuilder(64);
	
	private int line = 1, column = 0;
	
	Lexer(Reader source) {
		if (source == null) {
			throw new IllegalArgumentException("null");
		}
		
		JavaSourceReader jsr;
		try {
			jsr = wrapReader(source);
		} catch (IOException e) {
			logger.error("Failed to initialize JavaSourceReader.", e);
			jsr = createMockSource(new StringReader(""));
		}
		
		this.reader = jsr;
	}
	
	private TokenImpl characterLiteral() throws IOException {
		int read = reader.read();
		while (read != '\'' && !end(read) && (read != '\\' || !end(read = escapeSequence()))) {
			read = reader.read();
		}
		
		if (read == '\n') {
			logError("Character literal contains a line feed, assuming end of literal");
		}
		
		return token(Token.Kind.CHARACTER_LITERAL);
	}
	
	private boolean end(int c) {
		return c == '\n' || c < 0;
	}
	
	private EOFToken eof() {
		int line = reader.getLine();
		int column = reader.getColumn();
		
		return new EOFToken(line, column);
	}
	
	/**
	 * Reads the second character of an escape character, logging an error if it is no valid escape
	 * sequence.
	 * 
	 * @return the read character
	 * @throws IOException
	 */
	private int escapeSequence() throws IOException {
		final int valid = reader.lookAhead('\\', '\'', '"', 'n', 'r', 't', 'f', 'b');
		
		if (valid >= 0) {
			return valid;
		}
		
		logError("Illegal escape sequence \\" +
		    (reader.lookAhead() < 0 ? "<EOF>" : String.valueOf((char) reader.lookAhead())));
		return reader.read();
	}
	
	/**
	 * 
	 * @return the next token in the source
	 */
	AbstractTokenImpl getNextToken() {
		try {
			if (reader.lookAhead() == -1) {
				reader.close();
				return eof();
			}
			
			line = reader.getLine();
			column = reader.getColumn();
			image.setLength(0);
			final char firstChar = (char) reader.read();
			
			if (Character.isWhitespace(firstChar)) {
				return whitespace();
			} else if (Character.isJavaIdentifierStart(firstChar)) {
				return identifier();
			} else if (Character.digit(firstChar, 10) >= 0) {
				return numericalLiteral(false);
			} else if (firstChar == '"') {
				return stringLiteral();
			} else if (firstChar == '\'') {
				return characterLiteral();
			} else {
				return token(Token.Kind.SYMBOL);
			}
		} catch (IOException e) {
			logger.error("JavaSourceReader threw an IOException; pretending to have reached EOF.", e);
			return eof();
		}
	}
	
	private TokenImpl identifier() throws IOException {
		while (Character.isJavaIdentifierPart(reader.lookAhead())) {
			reader.read();
		}
		
		final Token.Kind kind = reservedWords.get(image.toString());
		return token(kind == null ? Token.Kind.IDENTIFIER : kind);
	}
	
	private void logError(String error) {
		logger.error(error + " [" + line + ":" + column + "-" + reader.getLine() + ":" + reader.getColumn() + "]");
	}
	
	private TokenImpl numericalLiteral(final boolean fraction) throws IOException {
		while (Character.digit(reader.lookAhead(), 10) >= 0) {
			reader.read();
		}
		
		if (!fraction && reader.lookAhead('.')) {
			if (Character.digit(reader.lookAhead(), 10) >= 0) {
				return numericalLiteral(true);
			} else {
				reader.backtrack();
			}
		}
		
		return token(Token.Kind.NUMERICAL_LITERAL);
	}
	
	private TokenImpl stringLiteral() throws IOException {
		int read = reader.read();
		while (read != '"' && !end(read) && (read != '\\' || !end(read = escapeSequence()))) {
			read = reader.read();
		}
		
		if (read == '\n') {
			logger.error("String literal contains a line feed; assuming end of literal.");
		}
		
		return token(Token.Kind.STRING_LITERAL);
	}
	
	private TokenImpl token(Token.Kind kind) {
		return new TokenImpl(this, kind, line, column, image.toString(), reader.getLine(), reader.getColumn());
	}
	
	private AbstractTokenImpl whitespace() throws IOException {
		while (Character.isWhitespace(reader.lookAhead())) {
			reader.read();
		}
		
		return token(Token.Kind.WHITESPACE);
	}
	
	private JavaSourceReader wrapReader(Reader source) throws IOException {
		return new JavaSourceReader(source) {
			@Override
			public void backtrack() {
				super.backtrack();
				
				image.setLength(image.length() - 1);
			}
			
			@Override
			public int read() throws IOException {
				int read = super.read();
				
				if (read >= 0) {
					image.append((char) read);
				}
				
				return read;
			}
		};
	}
}