package de.benshu.cofi.parser;

import de.benshu.commons.core.Pair;

public enum PairFactory implements Factory {
	INSTANCE;
	
	public static PairFactory get() {
		return INSTANCE;
	}
	
	PairFactory() {}
	
	@Override
	public Pair<?, ?> create(Object... args) {
		return Pair.of(args[0], args[1]);
	}
}
