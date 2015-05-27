package de.benshu.cofi.types.impl.intersections;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import de.benshu.cofi.types.IntersectionType;
import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.ProperTypeVisitor;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.bound.Type;
import de.benshu.cofi.types.impl.AbstractUnboundProperType;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutable;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.Tagged;

import static de.benshu.commons.core.streams.Collectors.set;

public class AnonymousIntersectionType<X extends TypeSystemContext<X>>
        extends AbstractIntersectionType<X, AnonymousIntersectionType<X>>
        implements Substitutable<X, AnonymousIntersectionType<X>> {

    public static <X extends TypeSystemContext<X>> ProperTypeMixin<X, ?> createIfNonTrivial(X context, AbstractTypeList<X, ProperTypeMixin<X, ?>> elements) {
        return elements.size() == 1
                ? elements.get(0)
                : create(context, elements);
    }

    public static <X extends TypeSystemContext<X>> AnonymousIntersectionType<X> create(X context, AbstractTypeList<X, ProperTypeMixin<X, ?>> elements) {
        return new AnonymousIntersectionType<>(context, elements, TagCombiners.intersect(elements.stream().map(Tagged::getTags).collect(set())));
    }

    private final AbstractTypeList<X, ProperTypeMixin<X, ?>> elements;

    private AnonymousIntersectionType(X context, AbstractTypeList<X, ProperTypeMixin<X, ?>> elements, Tagger tagger) {
        super(context, tagger);

        this.elements = elements;
    }

    @Override
    public AnonymousIntersectionType<X> substitute(Substitutions<X> substitutions) {
        return new AnonymousIntersectionType<>(
                getContext(), getElements().substituteUnchecked(substitutions),
                TagCombiners.substitute(getTags()));
    }

    @Override
    public AbstractTypeList<X, ProperTypeMixin<X, ?>> getElements() {
        return elements;
    }

    @Override
    public AnonymousIntersectionType<X> setTags(IndividualTags tags) {
        return new AnonymousIntersectionType<>(getContext(), elements, i -> HashTags.create(i, getTags().getIndividualTags().setAll(tags)));
    }

    @Override
    public Unbound<X> unbind() {
        return new Unbound<>(this);
    }

    @Override
    public String toDescriptor() {
        return CharMatcher.WHITESPACE.removeFrom(debug());
    }

    @Override
    public String debug() {
        return "(" + Joiner.on(" & ").join(getElements().mapAny(Type::debug)) + ")";
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundProperType<X, AnonymousIntersectionType<X>>
            implements IntersectionType {

        public Unbound(AnonymousIntersectionType<X> unbound) {
            super(unbound);
        }

        @Override
        public TypeList<? extends ProperType> getElements() {
            return bound.getElements().unbind();
        }

        @Override
        public <T> T accept(ProperTypeVisitor<T> visitor) {
            return visitor.visitIntersectionType(this);
        }
    }
}
