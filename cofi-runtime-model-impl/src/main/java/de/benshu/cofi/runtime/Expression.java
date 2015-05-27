package de.benshu.cofi.runtime;

import de.benshu.cofi.types.ProperType;

public interface Expression extends ModelNode {
    ProperType getType();

    <R> R accept(ExpressionVisitor<R> visitor);
}
