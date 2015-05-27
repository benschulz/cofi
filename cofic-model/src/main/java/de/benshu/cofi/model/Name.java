package de.benshu.cofi.model;

import de.benshu.cofi.types.bound.Member;
import de.benshu.cofi.types.bound.Type;

public interface Name<X> extends ModelNode<X> {
	interface Referent<X> {
		ModelNode<X> getNode();
		
		Type<X, ?> getType(X pass);
		
		boolean isMember();
		
		Member<X> getMember();
	}
	
	Referent<X> getReferent();
}
