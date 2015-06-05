package de.benshu.cofi.cofic.frontend.infer;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.types.impl.ProperTypeMixin;

public interface InferFunctionInvocation<T> {
    int getArgCount();

    // TODO the naming is backwards, it's the "would-be-explicit" signature type
    T setSignature(int index, ProperTypeMixin<Pass, ?> explicit, ProperTypeMixin<Pass, ?> implicit, T aggregate);
}