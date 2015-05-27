package de.benshu.cofi.types;

import com.google.common.collect.*;

public interface Method extends Member {
    Signature getRootSignature();

    ImmutableList<? extends Signature> getSignatures();

    interface Signature {
        TypeConstructor<? extends ProperType> getType();
    }
}
