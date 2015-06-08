package de.benshu.cofi.types.impl.declarations.source;

import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.tags.IndividualTags;

public interface SourceMemberDescriptor<X extends TypeSystemContext<X>> {
    MemberSort getSort();

    String getName();

    default IndividualTags getTags(X context) {
        return IndividualTags.empty();
    }
}
