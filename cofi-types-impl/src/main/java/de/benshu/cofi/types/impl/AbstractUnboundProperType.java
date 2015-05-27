package de.benshu.cofi.types.impl;

import com.google.common.collect.ImmutableMap;

import de.benshu.cofi.types.Member;
import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.ProperTypeSort;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.commons.core.Optional;
import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.commons.core.streams.Collectors.map;

public abstract class AbstractUnboundProperType<X extends TypeSystemContext<X>, U extends ProperTypeMixin<X, U>>
        extends AbstractUnboundType<X, U>
        implements ProperType {

    public AbstractUnboundProperType(U unbound) {
        super(unbound);
    }

    @Override
    public final ProperTypeSort getSort() {
        return bound.getSort();
    }

    @Override
    public final Optional<? extends Member> lookupMember(String name) {
        return bound.lookupMember(name).map(AbstractMember::unbind);
    }

    @Override
    public final ImmutableMap<String, ? extends Member> getMembers() {
        return bound.getMembers().entrySet().stream()
                .map(e -> immutableEntry(e.getKey(), e.getValue().unbind()))
                .collect(map());
    }
}
