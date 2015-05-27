package de.benshu.cofi.cofic.frontend.constraints;

import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.GenericModelData;
import de.benshu.cofi.model.impl.TypeExpression;
import de.benshu.cofi.model.impl.TypeParameters;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;

public class ConstraintsData extends GenericModelData {
    public static ConstraintsDataBuilder builder() {
        return new ConstraintsDataBuilder();
    }

    public final ImmutableMap<TypeParameters<Pass>, AbstractConstraints<Pass>> typeParameterConstraints;

    ConstraintsData(
            ImmutableMap<TypeExpression<Pass>, TypeMixin<Pass, ?>> typeExpressionTypes,
            ImmutableMap<TypeParameters<Pass>, AbstractConstraints<Pass>> typeParameterConstraints) {
        super(typeExpressionTypes);

        this.typeParameterConstraints = typeParameterConstraints;
    }
}
