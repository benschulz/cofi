package de.benshu.cofi.cofic.frontend.implementations;

import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.GenericModelData;
import de.benshu.cofi.cofic.frontend.namespace.AbstractResolution;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.model.impl.ModelNodeMixin;
import de.benshu.cofi.model.impl.NameExpression;
import de.benshu.cofi.model.impl.NameImpl;
import de.benshu.cofi.model.impl.TypeExpression;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

public class ImplementationData extends GenericModelData {
    public static ImplementationDataBuilder builder() {
        return new ImplementationDataBuilder(GenericModelData.empty());
    }

    public final ImmutableMap<ModelNodeMixin<Pass>, ModelNodeMixin<Pass>> transformations;
    public final ImmutableMap<NameExpression<Pass>, AbstractResolution> nameResolutions;
    public final ImmutableMap<NameImpl<Pass>, AbstractTypeList<Pass, ?>> nameTypeArguments;
    public final ImmutableMap<ExpressionNode<Pass>, ProperTypeMixin<Pass, ?>> expressionTypes;

    ImplementationData(
            ImmutableMap<TypeExpression<Pass>, TypeMixin<Pass, ?>> typeExpressionTypes,
            ImmutableMap<ModelNodeMixin<Pass>, ModelNodeMixin<Pass>> transformations,
            ImmutableMap<NameExpression<Pass>, AbstractResolution> nameResolutions,
            ImmutableMap<NameImpl<Pass>, AbstractTypeList<Pass, ?>> nameTypeArguments,
            ImmutableMap<ExpressionNode<Pass>, ProperTypeMixin<Pass, ?>> expressionTypes) {
        super(typeExpressionTypes);

        this.transformations = transformations;
        this.nameResolutions = nameResolutions;
        this.nameTypeArguments = nameTypeArguments;
        this.expressionTypes = expressionTypes;
    }
}
