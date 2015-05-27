package de.benshu.cofi.parser;

import com.google.common.collect.ImmutableList;
import de.benshu.commons.core.Pair;

public abstract class ListPairFactory<T, A, B> implements Factory {
	@SuppressWarnings("unchecked")
	@Override
	public T create(Object... args) {
		assert args.length == 1;
		
		return create((Pair<ImmutableList<A>, ImmutableList<B>>) args[0]);
	}
	
	public abstract T create(Pair<ImmutableList<A>, ImmutableList<B>> pair);
}
