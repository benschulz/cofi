package de.benshu.cofi.types.impl.declarations.source;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.impl.TypeSystemContext;

public interface SourcePropertyDescriptor<X extends TypeSystemContext<X>> extends SourceMemberDescriptor<X> {
    SourceType<X> getValueType();

    ImmutableList<SourceType<X>> getTraits();

    @Override
    default MemberSort getSort() {
        return MemberSort.PROPERTY;
    }
}
