package de.benshu.cofi.parser.lexer.impl;

final class TokenImpl extends AbstractTokenImpl {
	private final Kind kind;
	
	private Lexer lexer;
	
	private AbstractTokenImpl next;
	
	TokenImpl(Lexer lexer, Kind kind, int beginLine, int beginColumn, String image, int endLine, int endColumn) {
		super(beginLine, beginColumn, image, endLine, endColumn);
		this.lexer = lexer;
		this.kind = kind;
	}
	
	@Override
	public Kind getKind() {
		return kind;
	}
	
	@Override
	public AbstractTokenImpl next() {
		if (next == null) {
			next = lexer.getNextToken();
			lexer = null;
		}
		return next;
	}
}