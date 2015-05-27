package de.benshu.cofi.types;

import com.google.common.collect.*;
import de.benshu.commons.core.Optional;

public interface ProperType extends Type {
    <T> T accept(ProperTypeVisitor<T> visitor);

    ProperTypeSort getSort();

    Optional<? extends Member> lookupMember(String name);

    ImmutableMap<String, ? extends Member> getMembers();
}
