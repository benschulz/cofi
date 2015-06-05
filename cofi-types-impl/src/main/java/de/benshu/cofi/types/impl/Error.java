package de.benshu.cofi.types.impl;

import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.types.ProperTypeSort;
import de.benshu.cofi.types.bound.ErrorType;
import de.benshu.cofi.types.bound.ProperTypeVisitor;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.constraints.Monosemous;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Optional;

public class Error<X extends TypeSystemContext<X>> implements ProperTypeMixin<X, Error<X>>, ErrorType<X, Error<X>>, ConstructedTypeMixin<X, Error<X>, ErrorConstructor<X>> {
    public static <X extends TypeSystemContext<X>> Error<X> create() {
        return create(IndividualTags.empty());
    }

    public static <X extends TypeSystemContext<X>> Error<X> create(IndividualTags individualTags) {
        return new Error<>(individualTags);
    }

    private final Tags tags;

    Error(Tagger tagger) {
        this.tags = tagger.tag(this);
    }

    private Error(IndividualTags individualTags) {
        this.tags = HashTags.create(this, individualTags);
    }

    @Override
    public boolean isSameAs(TypeMixin<X, ?> other) {
        return other instanceof Error;
    }

    @Override
    public Error<X> substitute(Substitutions<X> substitutions) {
        return this;
    }

    @Override
    public AbstractTypeList<X, ?> getArguments() {
        throw null;
    }

    @Override
    public ErrorConstructor<X> getConstructor() {
        throw null;
    }

    @Override
    public Optional<AbstractMember<X>> lookupMember(String name) {
        throw null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ImmutableMap<String, AbstractMember<X>> getMembers() {
        throw null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> T accept(ProperTypeVisitor<X, T> visitor) {
        return visitor.visitErrorType(this);
    }

    @Override
    public ProperTypeSort getSort() {
        return ProperTypeSort.ERROR;
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public Error<X> setTags(IndividualTags tags) {
        return new Error<>(error -> getTags().setAll(tags));
    }

    @Override
    public AbstractConstraints<X> establishSubtypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return cs;
    }

    @Override
    public AbstractConstraints<X> establishSupertypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return cs;
    }

    @Override
    public de.benshu.cofi.types.Error unbind() {
        return new Unbound<>(this);
    }

    @Override
    public String toDescriptor() {
        return "<<error>>";
    }

    @Override
    public String debug() {
        return toDescriptor();
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundProperType<X, Error<X>>
            implements de.benshu.cofi.types.Error {

        public Unbound(Error<X> unbound) {
            super(unbound);
        }

        @Override
        public <T> T accept(de.benshu.cofi.types.ProperTypeVisitor<T> visitor) {
            return visitor.visitErrorType(this);
        }
    }
}
