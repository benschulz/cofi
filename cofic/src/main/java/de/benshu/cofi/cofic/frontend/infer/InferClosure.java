package de.benshu.cofi.cofic.frontend.infer;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

public interface InferClosure {
    void setSignature(ProperTypeMixin<Pass, ?> signature);

    AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>> getParameterTypes();
}
