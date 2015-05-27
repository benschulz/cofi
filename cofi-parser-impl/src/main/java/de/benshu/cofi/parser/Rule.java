package de.benshu.cofi.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.commons.core.Pair;

// TODO rewrite list factory methods to produce left recursive productions
public class Rule {
	private static final class ListFactory implements Factory {
		@Override
		public ImmutableList<?> create(Object... args) {
			final ImmutableList<?> items = (ImmutableList<?>) args[0];
			final Object item = args[1];
			
			if (items != null) {
				final ImmutableList.Builder<Object> itemsBuilder = ImmutableList.builder();
				itemsBuilder.add(item);
				itemsBuilder.addAll(items);
				return itemsBuilder.build();
			} else if (item != null) {
				return ImmutableList.of(item);
			} else {
				return ImmutableList.of();
			}
		}
	}
	
	private static final class ListPairFactory implements Factory {
		@Override
		public Pair<ImmutableList<?>, ImmutableList<?>> create(Object... args) {
			final Pair<?, ?> lists = (Pair<?, ?>) args[0];
			final Object item = args[1];
			final Object sep = args[2];
			
			final ImmutableList.Builder<Object> itemsBuilder = ImmutableList.builder();
			final ImmutableList.Builder<Object> sepsBuilder = ImmutableList.builder();
			
			if (sep != null) {
				itemsBuilder.add(item);
				sepsBuilder.add(sep);
			} else if (item != null) {
				itemsBuilder.add(item);
			}
			
			if (lists != null) {
				itemsBuilder.addAll((ImmutableList<?>) lists.a);
				sepsBuilder.addAll((ImmutableList<?>) lists.b);
			}
			
			return Pair.<ImmutableList<?>, ImmutableList<?>> of(itemsBuilder.build(), sepsBuilder.build());
		}
	}
	
	public static Rule create(NonTerminal nonTerminal, ImmutableList<Symbol> production, int... argIndices) {
		ImmutableList.Builder<Integer> builder = ImmutableList.builder();
		
		for (int i : argIndices) {
			builder.add(i);
		}
		
		return new Rule(nonTerminal, production, builder.build());
	}
	
	public static ImmutableSet<Rule> createList(NonTerminal nonTerminal, NonTerminal item, boolean allowNone) {
		return createListInternal(nonTerminal, item, null, allowNone);
	}
	
	public static ImmutableSet<Rule> createList(NonTerminal nonTerminal, NonTerminal item, Symbol terminator,
	    boolean allowNone) {
		return createListInternal(nonTerminal, item, terminator, allowNone);
	}
	
	private static ImmutableSet<Rule> createListInternal(NonTerminal nonTerminal, NonTerminal item, Symbol terminator,
	    boolean allowNone) {
		final NonTerminal auxiliary = NonTerminal.create("_LIST_[" + nonTerminal.name + "]", new ListFactory());
		
		final Rule noneRule = create(auxiliary, terminator == null ? production() : production(terminator), 0, 0);
		final Rule singleRule = create(auxiliary, terminator == null ? production(item) : production(item, terminator), 0,
		    1);
		final Rule singlePlusRule = create(auxiliary, production(item, auxiliary), 2, 1);
		
		final Rule adapterRule = create(nonTerminal, production(auxiliary), 1);
		
		if (allowNone) {
			return ImmutableSet.of(noneRule, singlePlusRule, adapterRule);
		} else {
			return ImmutableSet.of(singleRule, singlePlusRule, adapterRule);
		}
	}
	
	public static Rule createPassThrough(NonTerminal nonTerminal, NonTerminal production) {
		return Rule.create(nonTerminal, production(production), 1);
	}
	
	public static ImmutableSet<Rule> createSeparatedList(NonTerminal nonTerminal, Symbol item, Symbol separator,
	    boolean allowNone) {
		final NonTerminal auxiliary0 = NonTerminal.create("_SEP_LIST_1_[" + nonTerminal.name + "]", new ListPairFactory());
		final NonTerminal auxiliary1 = NonTerminal.create("_SEP_LIST_2_[" + nonTerminal.name + "]", new ListPairFactory());
		
		final Rule noneRule = create(auxiliary0, production(), 0, 0, 0);
		final Rule singleRule = create(auxiliary0, production(item), 0, 1, 0);
		final Rule singlePlusRule = create(auxiliary0, production(item, auxiliary1), 2, 1, 0);
		
		final Rule lastRule = create(auxiliary1, production(separator, item), 0, 2, 1);
		final Rule inbetweenRule = create(auxiliary1, production(separator, item, auxiliary1), 3, 2, 1);
		
		final Rule adapterRule = create(nonTerminal, production(auxiliary0), 1);
		
		if (allowNone) {
			return ImmutableSet.of(noneRule, singleRule, singlePlusRule, lastRule, inbetweenRule, adapterRule);
		} else {
			return ImmutableSet.of(singleRule, singlePlusRule, lastRule, inbetweenRule, adapterRule);
		}
	}

	private static ImmutableList<Symbol> production(Symbol... symbols) {
		return ImmutableList.copyOf(symbols);
	}
	
	public final NonTerminal nonTerminal;
	public final ImmutableList<Symbol> production;
	public final ImmutableList<Integer> argIndices;
	
	private Rule(NonTerminal nonTerminal, ImmutableList<Symbol> production, ImmutableList<Integer> args) {
		this.nonTerminal = nonTerminal;
		this.production = production;
		this.argIndices = args;
		
		for (int i : args) {
			if (i > production.size()) {
				throw new IllegalArgumentException("Argument index out of bounds: " + i);
			}
		}
	}
	
	@Override
	public String toString() {
		return nonTerminal + " -> " + production;
	}
}
