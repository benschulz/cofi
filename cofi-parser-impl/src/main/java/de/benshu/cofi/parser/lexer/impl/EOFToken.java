package de.benshu.cofi.parser.lexer.impl;

import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.parser.lexer.TokenString;

final class EOFToken extends AbstractTokenImpl {
	public EOFToken(int line, int column) {
		super(line, column, "", line, column);
	}
	
	@Override
	public Kind getKind() {
		return Kind.EOF;
	}
	
	@Override
	public TokenString getTokenString(Token successor) {
		if (successor != this) {
			throw new IllegalArgumentException("Not a successor: " + successor);
		}
		
		return this;
	}
	
	@Override
	AbstractTokenImpl next() {
		return null;
	}
}