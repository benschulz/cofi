package de.benshu.cofi.types.impl;

import com.google.common.collect.ImmutableMap;

import de.benshu.cofi.types.ProperTypeSort;
import de.benshu.cofi.types.bound.ProperTypeVisitor;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.constraints.Monosemous;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Optional;

public class Bottom<X extends TypeSystemContext<X>> implements ProperTypeMixin<X, Bottom<X>>, de.benshu.cofi.types.bound.Bottom<X, Bottom<X>> {
    public static <X extends TypeSystemContext<X>> Bottom<X> create() {
        return new Bottom<>();
    }

    private final Tags tags;

    public Bottom(Tags tags) {
        this.tags = tags;
    }

    public Bottom() {
        this.tags = HashTags.createEmpty(this);
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public Bottom<X> setTags(IndividualTags tags) {
        return new Bottom<>(getTags().setAll(tags));
    }

    @Override
    public Bottom<X> substitute(Substitutions<X> substitutions) {
        return this;
    }

    @Override
    public Optional<AbstractMember<X>> lookupMember(String name) {
        throw null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProperTypeSort getSort() {
        return ProperTypeSort.NOTHING;
    }

    // TODO make abstract
    public ImmutableMap<String, AbstractMember<X>> getMembers() {
        throw null;
    }

    @Override
    public <T> T accept(ProperTypeVisitor<X, T> visitor) {
        return visitor.visitBottomType(this);
    }

    @Override
    public String debug() {
        return toDescriptor();
    }

    @Override
    public String toDescriptor() {
        return "\u22A5";
    }

    @Override
    public AbstractConstraints<X> establishSubtypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return cs;
    }

    @Override
    public AbstractConstraints<X> establishSupertypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return other.establishSubtype(this, cs);
    }

    @Override
    public de.benshu.cofi.types.Bottom unbind() {
        return new Unbound<>(this);
    }

    private static class Unbound<X extends TypeSystemContext<X>> extends AbstractUnboundProperType<X, Bottom<X>> implements de.benshu.cofi.types.Bottom {
        public Unbound(Bottom<X> unbound) {
            super(unbound);
        }

        @Override
        public <T> T accept(de.benshu.cofi.types.ProperTypeVisitor<T> visitor) {
            return visitor.visitBottomType(this);
        }
    }
}