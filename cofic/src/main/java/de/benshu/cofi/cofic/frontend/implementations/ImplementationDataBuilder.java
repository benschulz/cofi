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
import de.benshu.cofi.model.impl.Statement;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.commons.core.exception.UnexpectedBranchException;

import java.util.Map;

public class ImplementationDataBuilder extends GenericModelDataBuilder<ImplementationDataBuilder, ImplementationData> {
    private final ImmutableMap.Builder<Statement<Pass>, Statement<Pass>> statementTransformations = ImmutableMap.builder();
    private final ImmutableMap.Builder<ExpressionNode<Pass>, ExpressionNode<Pass>> expressionTransformations = ImmutableMap.builder();
    private final ImmutableMap.Builder<NameExpression<Pass>, AbstractResolution> nameResolutions = ImmutableMap.builder();
    private final ImmutableMap.Builder<NameImpl<Pass>, AbstractTypeList<Pass, ?>> nameTypeArguments = ImmutableMap.builder();
    private final Map<ExpressionNode<Pass>, ProperTypeMixin<Pass, ?>> expressionTypes = Maps.newHashMap();

    ImplementationDataBuilder(GenericModelData genericModelData) {
        super(genericModelData);
    }

    @Override
    protected ImplementationDataBuilder self() {
        return this;
    }

    public ImplementationDataBuilder addAll(ImplementationDataBuilder other) {
        statementTransformations.putAll(other.statementTransformations.build());
        expressionTransformations.putAll(other.expressionTransformations.build());
        nameResolutions.putAll(other.nameResolutions.build());
        nameTypeArguments.putAll(other.nameTypeArguments.build());
        expressionTypes.putAll(other.expressionTypes);

        return super.addAll(other);
    }

    public ImplementationDataBuilder defineTransformation(Statement<Pass> untransformed, Statement<Pass> transformed) {
        this.statementTransformations.put(untransformed, transformed);

        return this;
    }

    public ImplementationDataBuilder defineTransformation(ExpressionNode<Pass> untransformed, ExpressionNode<Pass> transformed) {
        this.expressionTransformations.put(untransformed, transformed);

        return this;
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

    public ProperTypeMixin<Pass, ?> lookUpTypeOf(ExpressionNode<Pass> expression) {
        return expressionTypes.computeIfAbsent(expression, k -> { throw new UnexpectedBranchException(); });
    }

    public ImplementationDataBuilder copy() {
        return new ImplementationDataBuilder(new GenericModelData(ImmutableMap.of())).addAll(this);
    }

    public ImplementationData build() {
        return new ImplementationData(
                buildTypeExpressionTypes(),
                statementTransformations.build(),
                expressionTransformations.build(),
                nameResolutions.build(),
                nameTypeArguments.build(),
                ImmutableMap.copyOf(expressionTypes)
        );
    }
}
