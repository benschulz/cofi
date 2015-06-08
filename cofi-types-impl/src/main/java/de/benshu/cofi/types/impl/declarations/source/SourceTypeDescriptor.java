package de.benshu.cofi.types.impl.declarations.source;

import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.MemberSort;

public interface SourceTypeDescriptor<X extends TypeSystemContext<X>> extends SourceMemberDescriptor<X> {
    SourceType<X> getType();

    @Override
    default MemberSort getSort() {
        return MemberSort.TYPE;
    }
}
