package de.benshu.cofi.parser.lexer.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.lexer.AbstractToken;
import de.benshu.cofi.parser.lexer.DefaultTokenString;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.parser.lexer.TokenString;

abstract class AbstractTokenImpl extends AbstractToken {
	AbstractTokenImpl(int beginLine, int beginColumn, String lexeme, int endLine, int endColumn) {
		super(beginLine, beginColumn, lexeme, endLine, endColumn);
	}
	
	private ImmutableList<Token> collectSuccessors(ImmutableList.Builder<Token> lb, Token until) {
		if (until == null) {
			throw new IllegalArgumentException("null");
		}
		
		lb.add(this);
		
		if (this == until) {
			return lb.build();
		} else if (next() == null) {
			throw new IllegalArgumentException("Not a successor: " + until);
		} else {
			return next().collectSuccessors(lb, until);
		}
	}
	
	private ImmutableList<Token> collectSuccessors(Token until) {
		return collectSuccessors(ImmutableList.<Token> builder(), until);
	}
	
	@Override
	public TokenString getTokenString(final Token successor) {
		return DefaultTokenString.create(collectSuccessors(successor));
	}
	
	/**
	 * 
	 * @return the token immediately following this one
	 */
	abstract AbstractTokenImpl next();
}