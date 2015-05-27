package de.benshu.cofi.parser.lexer;

import com.google.common.collect.ImmutableList;

public abstract class AbstractToken extends AbstractTokenString implements Token {
	private final int beginColumn;
	private final int beginLine;
	private final int endColumn;
	private final int endLine;
	
	private final String lexeme;
	
	protected AbstractToken(int beginLine, int beginColumn, String lexeme, int endLine, int endColumn) {
		if (lexeme == null) {
			throw new IllegalArgumentException("null");
		} else if (beginLine > endLine) {
			throw new IllegalArgumentException("beginLine = " + beginLine + " > " + endLine + " = endLine");
		} else if (beginLine == endLine && beginColumn > endColumn) {
			throw new IllegalArgumentException("beginColumn = " + beginColumn + " > " + endColumn + " = endColumn");
		}
		
		this.beginLine = beginLine;
		this.beginColumn = beginColumn;
		this.lexeme = lexeme;
		this.endLine = endLine;
		this.endColumn = endColumn;
	}
	
	@Override
	public int getBeginColumn() {
		return beginColumn;
	}
	
	@Override
	public int getBeginLine() {
		return beginLine;
	}
	
	@Override
	public int getEndColumn() {
		return endColumn;
	}
	
	@Override
	public int getEndLine() {
		return endLine;
	}
	
	@Override
	public Token getFirst() {
		return this;
	}
	
	@Override
	public Token getLast() {
		return this;
	}
	
	@Override
	public String getLexeme() {
		return lexeme;
	}
	
	@Override
	public ImmutableList<Token> getTokens() {
		return ImmutableList.<Token> of(this);
	}
	
	@Override
	public boolean isA(Kind kind) {
		return kind.getSpecificKinds().contains(getKind());
	}
}