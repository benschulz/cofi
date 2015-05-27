package de.benshu.cofi.cofic.frontend;

import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.TypeExpression;
import de.benshu.cofi.types.impl.TypeMixin;

public class GenericModelData { // TODO rename once the exact contents are clearer
    public static GenericModelData empty() {
        return new GenericModelData(ImmutableMap.of());
    }

    public final ImmutableMap<TypeExpression<Pass>, TypeMixin<Pass, ?>> typeExpressionTypes;

    public GenericModelData(ImmutableMap<TypeExpression<Pass>, TypeMixin<Pass, ?>> typeExpressionTypes) {
        this.typeExpressionTypes = typeExpressionTypes;
    }
}
