package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.ProperTypeConstructor;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.Tags;

// TODO higher kinds
public class ErrorConstructor<X extends TypeSystemContext<X>>
        implements ProperTypeConstructorMixin<X, ErrorConstructor<X>, Error<X>> {

    public static <X extends TypeSystemContext<X>> ErrorConstructor<X> create() {
        return create(IndividualTags.empty());
    }

    private static <X extends TypeSystemContext<X>> ErrorConstructor<X> create(IndividualTags individualTags) {
        return new ErrorConstructor<>(individualTags);
    }

    private final Tags tags;

    private ErrorConstructor(Tagger tagger) {
        this.tags = tagger.tag(this);
    }

    private ErrorConstructor(IndividualTags individualTags) {
        this.tags = HashTags.create(this, individualTags);
    }

    @Override
    public boolean isSameAs(TypeMixin<X, ?> other) {
        return other instanceof ErrorConstructor;
    }

    @Override
    public Error<X> apply(AbstractTypeList<X, ?> arguments) {
        return new Error<>(TagCombiners.apply(getTags()));
    }

    @Override
    public TypeConstructorMixin<X, ?, ?> substitute(Substitutions<X> substitutions) {
        return self();
    }

    @Override
    public TypeParameterListImpl<X> getParameters() {
        return TypeParameterListImpl.empty();
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public ErrorConstructor<X> setTags(IndividualTags tags) {
        return new ErrorConstructor<>(TagCombiners.setAll(this, tags));
    }

    @Override
    public ProperTypeConstructor<?> unbind() {
        throw null;
    }

    @Override
    public String toDescriptor() {
        return "<<error>>";
    }

    @Override
    public String debug() {
        return toDescriptor();
    }
}
