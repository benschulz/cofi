package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.impl.TypeSystemContext;

public interface SourceMethodDescriptor<X extends TypeSystemContext<X>> extends SourceMemberDescriptor<X> {
    ImmutableList<SourceMethodSignatureDescriptor<X>> getMethodSignatureDescriptors();

    @Override
    default MemberSort getSort() {
        return MemberSort.METHOD;
    }
}
