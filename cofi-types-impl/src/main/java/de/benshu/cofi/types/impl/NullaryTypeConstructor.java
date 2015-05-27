package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.Type;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.Tags;

import static com.google.common.base.Preconditions.checkArgument;

// TODO Aren't the AdHoc.* constructors enough?
public abstract class NullaryTypeConstructor<X extends TypeSystemContext<X>, S extends NullaryTypeConstructor<X, S, T>, T extends TypeMixin<X, ?>>
        extends AbstractTypeConstructor<X, S, T> {

    public static <X extends TypeSystemContext<X>, T extends ProperTypeMixin<X, ?>> Proper<X, T> create(T constructed) {
        return new Proper<>(constructed, HashTags::createEmpty);
    }

    public static <X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> TypeConstructorMixin<X, ?, T> create(T constructed) {
        return new Generic<>(constructed, HashTags::createEmpty);
    }

    final T constructed;
    final Tags tags;

    private NullaryTypeConstructor(T constructed, Tagger tagger) {
        super(constructed.getContext());

        this.constructed = constructed;
        this.tags = tagger.tag(this);
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public T apply(AbstractTypeList<X, ?> arguments) {
        checkArgument(arguments.isEmpty());

        return constructed;
    }

    @Override
    public TypeConstructorMixin<X, ?, ?> substitute(Substitutions<X> substitutions) {
        TypeMixin<X, ?> substituted = constructed.substitute(substitutions);
        return substituted == constructed ? this : NullaryTypeConstructor.create(substituted);
    }

    @Override
    public TypeParameterListImpl<X> getParameters() {
        return TypeParameterListImpl.empty();
    }

    @Override
    public String toDescriptor() {
        return constructed.toDescriptor();
    }

    private static class Generic<X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>>
            extends NullaryTypeConstructor<X, Generic<X, T>, T> {

        private Generic(T constructed, Tagger tagger) {
            super(constructed, tagger);
        }

        @Override
        public Generic<X, T> setTags(IndividualTags tags) {
            return new Generic<>(constructed, TagCombiners.setAll(this, tags));
        }

        @Override
        public Unbound<X> unbind() {
            return new Unbound<>(this);
        }

        private static class Unbound<X extends TypeSystemContext<X>>
                extends AbstractUnboundTypeConstructor<X, Generic<X, ?>, Type> {

            public Unbound(Generic<X, ?> unbound) {
                super(unbound);
            }

            @Override
            public Type apply(TypeList<?> arguments) {
                return bound.apply(AbstractTypeList.rebind(arguments)).unbind();
            }
        }
    }

    private static class Proper<X extends TypeSystemContext<X>, T extends ProperTypeMixin<X, ?>>
            extends NullaryTypeConstructor<X, Proper<X, T>, T>
            implements ProperTypeConstructorMixin<X, Proper<X, T>, T> {

        private Proper(T constructed, Tagger tagger) {
            super(constructed, tagger);
        }

        @Override
        public Proper<X, T> setTags(IndividualTags tags) {
            return new Proper<>(constructed, TagCombiners.setAll(this, tags));
        }

        @Override
        public Unbound<X> unbind() {
            return new Unbound<>(this);
        }

        private static class Unbound<X extends TypeSystemContext<X>>
                extends AbstractUnboundProperTypeConstructor<X, Proper<X, ?>, ProperType> {

            public Unbound(Proper<X, ?> unbound) {
                super(unbound);
            }

            @Override
            public ProperType apply(TypeList<?> arguments) {
                return bound.apply(AbstractTypeList.rebind(arguments)).unbind();
            }
        }
    }
}
