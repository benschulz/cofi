package de.benshu.cofi.model.impl;

import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeMixin;

public interface TransformationContext<X extends ModelContext<X>> {
    TypeMixin<X, ?> resolveType(Fqn fqn);

    TypeMixin<X, ?> resolve(String name);

    ProperTypeMixin<X, ?> lookUpTypeOf(ExpressionNode<X> expression);
}
