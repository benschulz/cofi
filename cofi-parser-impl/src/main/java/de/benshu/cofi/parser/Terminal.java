package de.benshu.cofi.parser;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.lexer.Token;

public class Terminal extends Symbol {
	public static final Terminal NEW_LINE = new Terminal(Token.Kind.WHITESPACE, "NEW_LINE") {
		@Override
		public int getTokenCount() {
			return 0;
		}
	};
	
	public static Terminal create(Token.Kind token, String lexeme) {
		return new Terminal(token, lexeme);
	}
	
	public final Token.Kind token;
	public final String lexeme;
	private final int tokenCount;
	
	private Terminal(Token.Kind token, String lexeme) {
		super(Kind.TERMINAL);
		
		this.token = token;
		this.lexeme = lexeme;
		this.tokenCount = token == Token.Kind.SYMBOL ? lexeme.length() : 1;
	}
	
	@Override
	public int getTokenCount() {
		return tokenCount;
	}
	
	@Override
	public boolean matches(ImmutableList<Token> tokens, int index, int length) {
		final Token first = tokens.get(index);
		
		if (length == 1) {
			return matches(first);
		} else if (tokenCount == length) {
			if (!multiMatch(first, 0)) {
				return false;
			}
			
			Token last = first;
			for (int i = 1; i < length; ++i) {
				final Token token = tokens.get(index + i);
				
				if (last.getEndLine() != token.getBeginLine() || last.getEndColumn() != token.getBeginColumn()
				    || !multiMatch(token, i)) {
					return false;
				}
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	public boolean matches(Token token) {
		return token.isA(this.token) && (lexeme == null || token.getLexeme().equals(lexeme));
	}
	
	private boolean multiMatch(Token token, final int index) {
		return token.getKind() == Token.Kind.SYMBOL && token.getLexeme().charAt(0) == lexeme.charAt(index);
	}
	
	@Override
	public String toString() {
		return lexeme == null ? "[" + token + "]" : "'" + lexeme + "'";
	}
}
