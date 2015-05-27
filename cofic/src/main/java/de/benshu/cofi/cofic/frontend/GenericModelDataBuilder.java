package de.benshu.cofi.cofic.frontend;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.TypeExpression;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeMixin;

import java.util.Map;

public abstract class GenericModelDataBuilder<S extends GenericModelDataBuilder<S, D>, D extends GenericModelData> extends AbstractDataBuilder<S, D> {
    private final Map<TypeExpression<Pass>, TypeMixin<Pass, ?>> typeExpressionTypes = Maps.newHashMap();

    protected GenericModelDataBuilder(GenericModelData genericModelData) {
        typeExpressionTypes.putAll(genericModelData.typeExpressionTypes);
    }

    protected GenericModelDataBuilder() { }

    public S defineTypeOf(TypeExpression<Pass> typeExpression, TypeMixin<Pass, ?> type) {
        typeExpressionTypes.put(typeExpression, type);
        return self();
    }

    protected ImmutableMap<TypeExpression<Pass>, TypeMixin<Pass, ?>> buildTypeExpressionTypes() {
        return ImmutableMap.copyOf(typeExpressionTypes);
    }

    protected abstract S self();

    public ProperTypeMixin<Pass, ?> lookUpProperTypeOf(TypeExpression<Pass> typeExpression) {
        return (ProperTypeMixin<Pass, ?>) lookUpTypeOf(typeExpression);
    }

    public TypeMixin<Pass, ?> lookUpTypeOf(TypeExpression<Pass> typeExpression) {
        return typeExpressionTypes.get(typeExpression);
    }

    public S addAll(S other) {
        typeExpressionTypes.putAll(((GenericModelDataBuilder<S, D>) other).typeExpressionTypes);

        return self();
    }
}
