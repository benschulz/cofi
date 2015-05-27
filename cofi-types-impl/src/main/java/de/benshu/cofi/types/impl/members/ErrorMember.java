package de.benshu.cofi.types.impl.members;

import de.benshu.cofi.types.Member;
import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.declarations.InterpretedMemberDescriptor;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.Tags;

public class ErrorMember<X extends TypeSystemContext<X>> extends AbstractMember<X> {
    private final ProperTypeMixin<X, ?> owner;
    private final String name;

    public ErrorMember(X context, ProperTypeMixin<X, ?> owner, String name) {
        super(context);

        this.owner = owner;
        this.name = name;
    }

    @Override
    public MemberSort getSort() {
        return MemberSort.PROPERTY;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ProperTypeMixin<X, ?> getOwner() {
        return owner;
    }

    @Override
    public Tags getTags() {
        return HashTags.createEmpty(this);
    }

    @Override
    public ProperTypeConstructorMixin<X, ?, ? extends ProperTypeMixin<X, ?>> getType() {
        throw null;
    }

    @Override
    public AbstractMember<X> intersectWith(AbstractConstraints<X> contextualConstraints, AbstractMember<X> otherMember) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractMember<X> refine(AbstractConstraints<X> contextualConstraints, InterpretedMemberDescriptor<X> descriptors) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractMember<X> bequest(ProperTypeConstructorMixin<X, ?, ?> child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractMember<X> substitute(ProperTypeMixin<X, ?> newOwner, Substitutions<X> substitutions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Member unbind() {
        return new Unbound<>(this);
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundMember<X, ErrorMember<X>> {

        public Unbound(ErrorMember<X> unbound) {
            super(unbound);
        }
    }
}
