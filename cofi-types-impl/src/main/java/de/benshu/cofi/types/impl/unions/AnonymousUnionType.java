package de.benshu.cofi.types.impl.unions;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.ProperTypeVisitor;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.UnionType;
import de.benshu.cofi.types.bound.Type;
import de.benshu.cofi.types.impl.AbstractUnboundProperType;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeConstructorInvocation;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.streams.Collectors.set;

public class AnonymousUnionType<X extends TypeSystemContext<X>>
        extends AbstractUnionType<X, AnonymousUnionType<X>> {

    public static <X extends TypeSystemContext<X>> AnonymousUnionType<X> create(X context, AbstractTypeList<X, ProperTypeMixin<X, ?>> elements) {
        return new AnonymousUnionType<>(context, elements, TagCombiners.unite(elements.stream().map(e -> e.getTags()).collect(set())));
    }

    private final AbstractTypeList<X, ProperTypeMixin<X, ?>> elements;

    public AnonymousUnionType(X context, AbstractTypeList<X, ProperTypeMixin<X, ?>> elements, Tagger tagger) {
        super(context, tagger);

        this.elements = elements;
    }

    @Override
    public boolean isSameAs(TypeMixin<X, ?> other) {
        return this == other;
    }

    @Override
    public AnonymousUnionType<X> substitute(Substitutions<X> substitutions) {
        return new AnonymousUnionType<>(getContext(), getElements().substituteUnchecked(substitutions), TagCombiners.substitute(getTags()));
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
    public AbstractTypeList<X, ProperTypeMixin<X, ?>> getElements() {
        return elements;
    }

    @Override
    public AnonymousUnionType<X> setTags(IndividualTags tags) {
        return new AnonymousUnionType<>(getContext(), elements, TagCombiners.setAll(this, tags));
    }

    @Override
    public java.util.Optional<TypeConstructorInvocation<X>> tryGetInvocationOf(TypeConstructorMixin<X, ?, ?> typeConstructor) {
        return getElements().stream()
                .map(e -> e.tryGetInvocationOf(typeConstructor))
                .reduce((a, b) -> a.flatMap(ai -> b.map(ai::combine)))
                .orElse(java.util.Optional.empty());
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
        return "(" + Joiner.on(" | ").join(getElements().mapAny(Type::debug)) + ")";
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundProperType<X, AnonymousUnionType<X>>
            implements UnionType {
        public Unbound(AnonymousUnionType<X> unbound) {
            super(unbound);
        }

        @Override
        public TypeList<? extends ProperType> getElements() {
            return bound.getElements().unbind();
        }

        @Override
        public <T> T accept(ProperTypeVisitor<T> visitor) {
            return visitor.visitUnionType(this);
        }
    }
}
