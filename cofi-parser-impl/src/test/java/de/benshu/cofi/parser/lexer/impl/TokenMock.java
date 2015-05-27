package de.benshu.cofi.parser.lexer.impl;

public class TokenMock extends AbstractTokenImpl {
	private final Kind kind;
	private final AbstractTokenImpl next;
	
	public TokenMock(Kind kind, int beginLine, int beginColumn, String lexeme, int endLine, int endColumn) {
		this(kind, beginLine, beginColumn, lexeme, endLine, endColumn, null);
	}
	
	public TokenMock(Kind kind, int beginLine, int beginColumn, String lexeme, int endLine, int endColumn,
	    AbstractTokenImpl next) {
		super(beginLine, beginColumn, lexeme, endLine, endColumn);
		
		this.kind = kind;
		this.next = next;
	}
	
	@Override
	public Kind getKind() {
		return kind;
	}
	
	@Override
	AbstractTokenImpl next() {
		return next;
	}
}
