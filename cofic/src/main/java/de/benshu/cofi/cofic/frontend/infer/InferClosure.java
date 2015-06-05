package de.benshu.cofi.cofic.frontend.infer;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

public interface InferClosure<T> {
    T setSignature(ProperTypeMixin<Pass, ?> signature, T aggregate);

    AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>> getParameterTypes();
}
