package de.benshu.cofi.types.bound;

import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.tags.Tags;

public interface Member<X> {
    MemberSort getSort();

    String getName();

    Tags getTags();

    TypeConstructor<X, ?, ? extends ProperType<X, ?>> getType();
}
