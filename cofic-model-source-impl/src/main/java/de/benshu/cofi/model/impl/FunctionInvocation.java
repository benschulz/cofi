package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.lexer.TokenString;
import de.benshu.cofi.types.impl.ProperTypeMixin;

public interface FunctionInvocation<X extends ModelContext<X>> extends ExpressionMixin<X> {
	ImmutableList<ExpressionNode<X>> getArgs();
	
	TokenString getSourceSnippet();

	interface Signature<X extends ModelContext<X>> {
		int getSignatureIndex();

		ProperTypeMixin<X,?> getExplicitSignatureType();

		ProperTypeMixin<X,?> getImplicitSignatureType();
	}
}
