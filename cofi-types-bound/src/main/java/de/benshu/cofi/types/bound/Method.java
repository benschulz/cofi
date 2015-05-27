package de.benshu.cofi.types.bound;

import com.google.common.collect.*;

public interface Method<X> extends Member<X> {
    Signature<X> getRootSignature();

    ImmutableList<? extends Signature<X>> getSignatures();

    interface Signature<X> {
        TypeConstructor<X, ?, ? extends ProperType<X, ?>> getType();
    }
}
