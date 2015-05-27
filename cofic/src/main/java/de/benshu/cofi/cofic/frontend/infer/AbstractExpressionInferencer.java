package de.benshu.cofi.cofic.frontend.infer;

import com.google.common.base.Preconditions;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.commons.core.Optional;

abstract class AbstractExpressionInferencer implements ExpressionInferencer {
    private final int typeArgCount;
    private Optional<ProperTypeMixin<Pass, ?>> specific;

    public AbstractExpressionInferencer(int typeArgCount) {
        this.typeArgCount = typeArgCount;
    }

    @Override
    public final int getTypeArgCount() {
        return typeArgCount;
    }

    @Override
    public final Optional<ProperTypeMixin<Pass, ?>> inferSpecific(Pass pass) {
        if (specific == null) {
            specific = doInferSpecific(pass);
            Preconditions.checkState(specific != null);
        }

        return specific;
    }

    abstract Optional<ProperTypeMixin<Pass, ?>> doInferSpecific(Pass pass);
}
