package de.benshu.cofi.cofic.frontend.infer;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.inference.Parametrization;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.commons.core.Optional;

public interface ExpressionInferencer {
    /**
     * @return the number of type args to be inferred for the expression
     */
    int getTypeArgCount();

    Optional<Parametrization<Pass>> inferGeneric(Pass pass, TypeParameterListImpl<Pass> params, int offset, AbstractConstraints<Pass> constraints, ProperTypeMixin<Pass, ?> context);

    Optional<ProperTypeMixin<Pass, ?>> inferSpecific(Pass pass);
}
