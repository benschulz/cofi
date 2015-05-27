package de.benshu.cofi.types.impl.intersections;

import de.benshu.cofi.types.IntersectionTypeConstructor;
import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.ProperTypeVisitor;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.bound.ConstructedIntersectionType;
import de.benshu.cofi.types.impl.AbstractUnboundConstructedType;
import de.benshu.cofi.types.impl.ConstructedTypeMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeVariableImpl;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;

import java.util.Collection;

public class ConstructedIntersectionTypeImpl<X extends TypeSystemContext<X>>
        extends AbstractIntersectionType<X, ConstructedIntersectionTypeImpl<X>>
        implements ConstructedTypeMixin<X, ConstructedIntersectionTypeImpl<X>, AbstractIntersectionTypeConstructor<X>>,
                   ConstructedIntersectionType<X, ConstructedIntersectionTypeImpl<X>, AbstractIntersectionTypeConstructor<X>> {

    private final AbstractIntersectionTypeConstructor<X> constructor;
    private final AbstractTypeList<X, ?> arguments;
    private final AbstractTypeList<X, ProperTypeMixin<X, ?>> elements;

    public ConstructedIntersectionTypeImpl(AbstractIntersectionTypeConstructor<X> constructor, AbstractTypeList<X, ?> arguments, AbstractTypeList<X, ProperTypeMixin<X, ?>> elements, Tagger tagger) {
        super(constructor.getContext(), tagger);

        this.constructor = constructor;
        this.arguments = arguments;
        this.elements = elements;
    }

    @Override
    public ConstructedIntersectionTypeImpl<X> substitute(Substitutions<X> substitutions) {
        return new ConstructedIntersectionTypeImpl<>(
                constructor,
                arguments.substitute(substitutions),
                elements.substituteUnchecked(substitutions),
                TagCombiners.substitute(getTags())
        );
    }

    @Override
    public AbstractTypeList<X, ?> getArguments() {
        return arguments;
    }

    @Override
    public AbstractIntersectionTypeConstructor<X> getConstructor() {
        return constructor;
    }

    @Override
    public AbstractTypeList<X, ProperTypeMixin<X, ?>> getElements() {
        return elements;
    }

    @Override
    public ConstructedIntersectionTypeImpl<X> setTags(IndividualTags tags) {
        return new ConstructedIntersectionTypeImpl<>(constructor, arguments, elements, t -> HashTags.create(t, getTags().getIndividualTags().setAll(tags)));
    }

    @Override
    public boolean containsAny(Collection<TypeVariableImpl<X, ?>> variables) {
        return super.containsAny(variables);
    }

    @Override
    public de.benshu.cofi.types.ConstructedIntersectionType unbind() {
        return new Unbound<>(this);
    }

    @Override
    public String toDescriptor() {
        return ConstructedTypeMixin.super.toDescriptor();
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundConstructedType<X, ConstructedIntersectionTypeImpl<X>, AbstractIntersectionTypeConstructor<X>, IntersectionTypeConstructor>
            implements de.benshu.cofi.types.ConstructedIntersectionType {

        public Unbound(ConstructedIntersectionTypeImpl<X> unbound) {
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

        @Override
        public IntersectionTypeConstructor getConstructor() {
            return bound.getConstructor().unbind();
        }
    }
}
