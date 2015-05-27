package de.benshu.cofi.parser.lexer;

import de.benshu.cofi.parser.lexer.Token.Kind;

import java.util.Set;

public class UnexpectedTokenException extends LexerException {
	private static final long serialVersionUID = -5591609644191236219L;
	
	private static String msg(Token token, Set<Kind> expected) {
		final String loc = "Parse error at " + token.getBeginLine() + ":" + token.getBeginColumn() + "-"
		    + token.getEndLine() + ":" + token.getEndColumn();
		final String exp0 = String.valueOf(expected);
		final String exp = "Expected " + exp0.substring(1, exp0.length() - 1);
		final String enc = "Encountered: " + token.getKind();
		final String img = "Image: " + token.getLexeme();
		
		return loc + "\n" + exp + "\n" + enc + "\n" + img;
	}
	
	public UnexpectedTokenException(Token token, Set<Token.Kind> expected) {
		super(msg(token, expected));
	}
}
