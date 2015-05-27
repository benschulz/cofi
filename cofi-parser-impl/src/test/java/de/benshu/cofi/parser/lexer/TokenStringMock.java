/**
 * 
 */
package de.benshu.cofi.parser.lexer;

import com.google.common.collect.ImmutableList;

class TokenStringMock extends AbstractTokenString {
	private final ImmutableList<Token> tokens;
	
	TokenStringMock(Token... tokens) {
		this.tokens = ImmutableList.copyOf(tokens);
	}
	
	@Override
	public int getBeginColumn() {
		return tokens.get(0).getBeginColumn();
	}
	
	@Override
	public int getBeginLine() {
		return tokens.get(0).getBeginLine();
	}
	
	@Override
	public int getEndColumn() {
		return tokens.get(tokens.size() - 1).getEndColumn();
	}
	
	@Override
	public int getEndLine() {
		return tokens.get(tokens.size() - 1).getEndLine();
	}
	
	@Override
	public Token getFirst() {
		return tokens.get(0);
	}
	
	@Override
	public Token getLast() {
		return tokens.get(tokens.size() - 1);
	}
	
	@Override
	public String getLexeme() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ImmutableList<Token> getTokens() {
		return tokens;
	}
}