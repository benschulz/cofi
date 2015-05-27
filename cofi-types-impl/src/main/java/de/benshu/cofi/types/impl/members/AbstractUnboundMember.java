package de.benshu.cofi.types.impl.members;

import de.benshu.cofi.types.ConstructedType;
import de.benshu.cofi.types.Member;
import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.TypeConstructor;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.tags.Tags;

public abstract class AbstractUnboundMember<X extends TypeSystemContext<X>, U extends AbstractMember<X>> implements Member {
    public final U unbound;

    public AbstractUnboundMember(U unbound) {
        this.unbound = unbound;
    }

    @Override
    public MemberSort getSort() {
        return unbound.getSort();
    }

    @Override
    public String getName() {
        return unbound.getName();
    }

    @Override
    public Tags getTags() {
        return unbound.getTags();
    }

    @Override
    public TypeConstructor<? extends ConstructedType<?>> getType() {
        return (TypeConstructor<? extends ConstructedType<?>>) unbound.getType().unbind();
    }
}
