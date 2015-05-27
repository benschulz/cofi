package de.benshu.cofi.types.bound;

import com.google.common.collect.*;
import de.benshu.cofi.types.ProperTypeSort;
import de.benshu.commons.core.Optional;

public interface ProperType<X, S extends ProperType<X, S>> extends Type<X, S> {
    <T> T accept(ProperTypeVisitor<X, T> visitor);

    ProperTypeSort getSort();

    Optional<? extends Member<X>> lookupMember(String name);

    ImmutableMap<String, ? extends Member<X>> getMembers();

}
