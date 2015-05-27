package de.benshu.cofi.parser.lexer.impl;

import com.google.common.base.Preconditions;
import de.benshu.cofi.parser.lexer.Token;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

public class LexerMock extends Lexer {
	private final List<AbstractTokenImpl> tokens = new LinkedList<AbstractTokenImpl>();
	private int index = 0;
	
	LexerMock() {
		super(new StringReader(""));
	}
	
	void addToken(Token.Kind kind, String image, int endLine, int endColumn) {
		checkState();
		
		tokens.add(new TokenImpl(this, kind, last().getEndLine(), last().getEndColumn(), image, endLine, endColumn));
	}
	
	private void checkState() {
		Preconditions.checkState(index <= tokens.size());
	}
	
	@Override
	AbstractTokenImpl getNextToken() {
		checkState();
		
		if (index == tokens.size()) {
			final EOFToken eof = new EOFToken(last().getEndLine(), last().getEndColumn());
			
			tokens.add(eof);
			index += 2;
			
			return eof;
		}
		
		return tokens.get(index++);
	}
	
	private Token last() {
		return tokens.size() > 0 ? tokens.get(tokens.size() - 1) : new EOFToken(1, 1);
	}
}
