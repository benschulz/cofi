package de.benshu.cofi.types.impl.unions;

import com.google.common.collect.ImmutableMap;

import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.ProperTypeVisitor;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.UnionTypeConstructor;
import de.benshu.cofi.types.impl.AbstractUnboundConstructedType;
import de.benshu.cofi.types.impl.ConstructedTypeMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.bound.ConstructedUnionType;
import de.benshu.commons.core.Optional;

public class ConstructedUnionTypeImpl<X extends TypeSystemContext<X>>
        extends AbstractUnionType<X, ConstructedUnionTypeImpl<X>>
        implements ConstructedTypeMixin<X, ConstructedUnionTypeImpl<X>, AbstractUnionTypeConstructor<X>>,
                   ConstructedUnionType<X, ConstructedUnionTypeImpl<X>, AbstractUnionTypeConstructor<X>> {

    private final AbstractUnionTypeConstructor<X> constructor;
    private final AbstractTypeList<X, ?> arguments;
    private final AbstractTypeList<X, ProperTypeMixin<X, ?>> elements;

    public ConstructedUnionTypeImpl(AbstractUnionTypeConstructor<X> constructor, AbstractTypeList<X, ?> arguments, AbstractTypeList<X, ProperTypeMixin<X, ?>> elements, Tagger tagger) {
        super(constructor.getContext(), tagger);

        this.constructor = constructor;
        this.arguments = arguments;
        this.elements = elements;
    }

    @Override
    public ConstructedUnionTypeImpl<X> substitute(Substitutions<X> substitutions) {
        return new ConstructedUnionTypeImpl<>(
                constructor,
                arguments.substitute(substitutions),
                elements.substituteUnchecked(substitutions),
                TagCombiners.substitute(getTags())
        );
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
    public AbstractTypeList<X, ?> getArguments() {
        return arguments;
    }

    @Override
    public AbstractUnionTypeConstructor<X> getConstructor() {
        return constructor;
    }

    @Override
    public AbstractTypeList<X, ProperTypeMixin<X, ?>> getElements() {
        return elements;
    }

    @Override
    public ConstructedUnionTypeImpl<X> setTags(IndividualTags tags) {
        return new ConstructedUnionTypeImpl<>(constructor, arguments, elements, TagCombiners.setAll(this, tags));
    }

    @Override
    public de.benshu.cofi.types.ConstructedUnionType unbind() {
        return new Unbound<>(this);
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundConstructedType<X, ConstructedUnionTypeImpl<X>, AbstractUnionTypeConstructor<X>, UnionTypeConstructor>
            implements de.benshu.cofi.types.ConstructedUnionType {

        public Unbound(ConstructedUnionTypeImpl<X> unbound) {
            super(unbound);
        }

        @Override
        public UnionTypeConstructor getConstructor() {
            return bound.getConstructor().unbind();
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
