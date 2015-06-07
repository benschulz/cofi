package de.benshu.cofi.model;

import de.benshu.cofi.types.MemberSort;

public interface MemberDeclaration<X> extends AnnotatedNode<X> {
	MemberSort getSort();
	
	String getName();
}
