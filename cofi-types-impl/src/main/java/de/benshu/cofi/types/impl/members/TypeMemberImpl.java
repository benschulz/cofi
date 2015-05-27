package de.benshu.cofi.types.impl.members;

import de.benshu.cofi.types.Member;
import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.declarations.InterpretedMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.InterpretedTypeDescriptor;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.Tags;

public class TypeMemberImpl<X extends TypeSystemContext<X>> extends AbstractMember<X> {
    public static <X extends TypeSystemContext<X>> TypeMemberImpl<X> createTypeMember(X context, ProperTypeMixin<X, ?> owner, InterpretedTypeDescriptor<X> descriptor) {
        return new TypeMemberImpl<>(context, owner, descriptor.getName(), m -> HashTags.create(m, descriptor.getTags(context)), descriptor.getType(context));
    }

    private final ProperTypeMixin<X, ?> owner;
    private final String name;
    private final Tags tags;
    private final ProperTypeConstructorMixin<X, ?, ?> type;

    private TypeMemberImpl(X context, ProperTypeMixin<X, ?> owner, String name, Tagger tagger, ProperTypeConstructorMixin<X, ?, ?> type) {
        super(context);

        this.owner = owner;
        this.name = name;
        this.tags = tagger.tag(this);
        this.type = type;
    }

    @Override
    public MemberSort getSort() {
        return MemberSort.TYPE;
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
        return tags;
    }

    @Override
    public ProperTypeConstructorMixin<X, ?, ?> getType() {
        return type;
    }

    @Override
    public AbstractMember<X> intersectWith(AbstractConstraints<X> contextualConstraints, AbstractMember<X> otherMember) {
        throw null;
    }

    @Override
    public AbstractMember<X> refine(AbstractConstraints<X> contextualConstraints, InterpretedMemberDescriptor<X> descriptors) {
        throw null;
    }

    @Override
    public AbstractMember<X> bequest(ProperTypeConstructorMixin<X, ?, ?> newOwner) {
        throw null;
    }

    @Override
    public AbstractMember<X> substitute(ProperTypeMixin<X, ?> newOwner, Substitutions<X> substitutions) {
        return new TypeMemberImpl<>(context, newOwner, name, TagCombiners.substitute(getTags()), type);
    }

    @Override
    public Member unbind() {
        return new Unbound<>(this);
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundMember<X, TypeMemberImpl<X>> {
        public Unbound(TypeMemberImpl<X> unbound) {
            super(unbound);
        }
    }
}
