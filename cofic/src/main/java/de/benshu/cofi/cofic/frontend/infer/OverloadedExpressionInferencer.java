package de.benshu.cofi.cofic.frontend.infer;

public interface OverloadedExpressionInferencer {
	// TODO maybe add a parameter to filter out some possibilities (like param=arg count)
	// TODO for gods sake, rename this.. "serialize" or "order" perhaps? 
	Iterable<ExpressionInferencer> unoverload();
}
