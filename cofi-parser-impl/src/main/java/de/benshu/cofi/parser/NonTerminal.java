package de.benshu.cofi.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.commons.core.Pair;

public class NonTerminal extends Symbol {
	public static NonTerminal create(String name, Factory factory) {
		return new NonTerminal(name, factory);
	}
	
	public static NonTerminal createEmptyList(String name) {
		return createEmptyList(name, Factory.FST);
	}
	
	public static NonTerminal createEmptyList(String name, final Factory factory) {
		return NonTerminal.create(name, args -> factory.create(ImmutableList.of()));
	}
	
	public static NonTerminal createPassThrough(String name) {
		return create(name, Factory.FST);
	}
	
	public static NonTerminal createReturnPairA(String name) {
		return createReturnPairA(name, Factory.FST);
	}
	
	public static NonTerminal createReturnPairA(String name, final Factory factory) {
		return create(name, args -> {
            Preconditions.checkArgument(args.length == 1);

            return factory.create(((Pair<?, ?>) args[0]).a);
        });
	}
	
	public final Factory factory;
	public final String name;
	private ImmutableSet<Rule> rules;
	
	NonTerminal(String name, Factory factory) {
		super(Kind.NONTERMINAL);
		
		this.name = name;
		this.factory = factory;
	}
	
	ImmutableSet<Rule> getRules() {
		return rules;
	}
	
	@Override
	public int getTokenCount() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean matches(ImmutableList<Token> tokens, int j, int i) {
		return false;
	}
	
	void setRules(ImmutableSet<Rule> rules) {
		if (this.rules != null) {
			throw new IllegalStateException("This non terminal already belongs to a grammar.");
		}
		
		this.rules = rules;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
