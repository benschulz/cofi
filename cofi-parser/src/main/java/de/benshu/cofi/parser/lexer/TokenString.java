package de.benshu.cofi.parser.lexer;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.notes.Source;

public interface TokenString extends Source.Snippet {
	Token getFirst();
	
	Token getLast();
	
	int getLength();
	
	String getLexeme();
	
	ImmutableList<Token> getTokens();
	
	TokenString substring(int from, int to);
}
