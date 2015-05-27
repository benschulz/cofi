package de.benshu.cofi.cofic.frontend.implementations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.GenericModelData;
import de.benshu.cofi.cofic.frontend.GenericModelDataBuilder;
import de.benshu.cofi.cofic.frontend.namespace.AbstractResolution;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.model.impl.NameExpression;
import de.benshu.cofi.model.impl.NameImpl;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.commons.core.exception.UnexpectedBranchException;

import java.util.Map;

public class ImplementationDataBuilder extends GenericModelDataBuilder<ImplementationDataBuilder, ImplementationData> {
    private final Map<ExpressionNode<Pass>, ProperTypeMixin<Pass, ?>> expressionTypes = Maps.newHashMap();
    private final ImmutableMap.Builder<NameImpl<Pass>, AbstractTypeList<Pass, ?>> nameTypeArguments = ImmutableMap.builder();
    private final ImmutableMap.Builder<NameExpression<Pass>, AbstractResolution> nameResolutions = ImmutableMap.builder();

    ImplementationDataBuilder(GenericModelData genericModelData) {
        super(genericModelData);
    }

    @Override
    protected ImplementationDataBuilder self() {
        return this;
    }

    public ImplementationDataBuilder addAll(ImplementationDataBuilder other) {
        nameResolutions.putAll(other.nameResolutions.build());
        nameTypeArguments.putAll(other.nameTypeArguments.build());
        expressionTypes.putAll(other.expressionTypes);

        return super.addAll(other);
    }

    public ImplementationDataBuilder defineResolutionOf(NameExpression<Pass> nameExpression, AbstractResolution resolution) {
        nameResolutions.put(nameExpression, resolution);

        return this;
    }

    public ImplementationDataBuilder defineTypeArgumentsTo(NameImpl<Pass> name, AbstractTypeList<Pass, ?> typeArguments) {
        nameTypeArguments.put(name, typeArguments);

        return this;
    }

    public ImplementationDataBuilder defineTypeOf(ExpressionNode<Pass> expression, ProperTypeMixin<Pass, ?> type) {
        expressionTypes.put(expression, type);

        return this;
    }

    public ProperTypeMixin<Pass, ?> lookUpProperTypeOf(ExpressionNode<Pass> expression) {
        return expressionTypes.computeIfAbsent(expression, k -> { throw new UnexpectedBranchException(); });
    }


    public ImplementationData build() {
        return new ImplementationData(
                buildTypeExpressionTypes(),
                nameResolutions.build(),
                nameTypeArguments.build(),
                ImmutableMap.copyOf(expressionTypes)
        );
    }
}
