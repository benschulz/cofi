package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeSystemContext;

public interface InterpretedMethodDescriptor<X extends TypeSystemContext<X>> extends InterpretedMemberDescriptor<X> {
    ImmutableList<InterpretedMethodSignatureDescriptor<X>> getMethodSignatureDescriptors();
}
