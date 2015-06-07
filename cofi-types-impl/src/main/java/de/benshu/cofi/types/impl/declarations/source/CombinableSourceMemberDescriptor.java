package de.benshu.cofi.types.impl.declarations.source;

import de.benshu.cofi.types.impl.TypeSystemContext;

public interface CombinableSourceMemberDescriptor<X extends TypeSystemContext<X>> extends SourceMemberDescriptor<X> {
    CombinableSourceMemberDescriptor<X> combineWith(CombinableSourceMemberDescriptor<X> other);
}
