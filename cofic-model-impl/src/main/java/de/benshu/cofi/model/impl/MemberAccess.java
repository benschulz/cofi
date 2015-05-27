package de.benshu.cofi.model.impl;

import de.benshu.cofi.types.impl.members.AbstractMember;

public interface MemberAccess<X extends ModelContext<X>> extends ExpressionMixin<X> {
	ExpressionMixin<X> getPrimary();
	
	AbstractMember<X> getMember(X pass);
}
