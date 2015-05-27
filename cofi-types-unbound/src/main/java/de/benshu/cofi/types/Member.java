package de.benshu.cofi.types;

import de.benshu.cofi.types.tags.Tags;

public interface Member {
    MemberSort getSort();

    String getName();

    Tags getTags();

    TypeConstructor<? extends ProperType> getType();

}
