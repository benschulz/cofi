package de.benshu.cofi.types.impl.members;

import com.google.common.collect.ImmutableList;

import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.bound.Property;
import de.benshu.cofi.types.impl.NullaryTypeConstructor;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.declarations.InterpretedMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.InterpretedPropertyDescriptor;
import de.benshu.cofi.types.impl.intersections.AnonymousIntersectionType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.Tags;
import static de.benshu.cofi.types.impl.lists.AbstractTypeList.typeList;

final class PropertyImpl<X extends TypeSystemContext<X>> extends AbstractMember<X> implements Property<X> {
    public static <X extends TypeSystemContext<X>> PropertyImpl<X> createProperty(X context, ProperTypeMixin<X, ?> owner, InterpretedPropertyDescriptor<X> descriptor) {
        PropertyImpl<X> empty = PropertyImpl.empty(context, descriptor.getName());
        return empty.refine(context, owner, descriptor, false);
    }

    private static <X extends TypeSystemContext<X>> PropertyImpl<X> empty(X context, String name) {
        // TODO remove and refactor createProperty -- empty(...).refine(...) does not work well
        return new PropertyImpl<>(context, name);
    }

    private final ProperTypeMixin<X, ?> owner;
    private final String name;

    private final ProperTypeMixin<X, ?> type;
    private final ImmutableList<AbstractTemplateTypeConstructor<X>> elements;

    private final Tags tags;

    private PropertyImpl(X context, String name) {
        super(context);

        this.owner = null;
        this.name = name;
        this.type = null;
        this.elements = ImmutableList.of();
        this.tags = HashTags.createEmpty(this);
    }

    public PropertyImpl(X context, ProperTypeMixin<X, ?> owner, String name, ProperTypeMixin<X, ?> type,
                        ImmutableList<AbstractTemplateTypeConstructor<X>> elements, Tagger tagger) {
        super(context);

        this.owner = owner;
        this.name = name;
        this.type = type;
        this.elements = elements;
        this.tags = tagger.tag(this);
    }

    @Override
    public MemberSort getSort() {
        return MemberSort.PROPERTY;
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
        return NullaryTypeConstructor.create(AnonymousIntersectionType.createIfNonTrivial(owner.getContext(), elements.stream()
                .map(e -> e.apply(AbstractTypeList.of(type)))
                .collect(typeList())));
    }

    @Override
    public PropertyImpl<X> intersectWith(AbstractConstraints<X> contextualConstraints, AbstractMember<X> otherMember) {
        throw null;
    }

    @Override
    public PropertyImpl<X> refine(AbstractConstraints<X> contextualConstraints, InterpretedMemberDescriptor<X> descriptor) {
        return refine(context, owner, descriptor, true);
    }

    PropertyImpl<X> refine(X context, ProperTypeMixin<X, ?> owner, InterpretedMemberDescriptor<X> descriptor, boolean refineTags) {
        final InterpretedPropertyDescriptor<X> pd = (InterpretedPropertyDescriptor<X>) descriptor;

        final ImmutableList.Builder<AbstractTemplateTypeConstructor<X>> builder = ImmutableList.builder();

        builder.addAll(elements);
        builder.addAll(pd.getTraits());

        // TODO make sure that the new type works out (variance/bounds of elements)
        return new PropertyImpl<>(context, owner, name, pd.getType(), builder.build(), refineTags
                ? TagCombiners.refine(getTags(), descriptor.getTags(context))
                : m -> HashTags.create(m, descriptor.getTags(context)));
    }

    @Override
    public PropertyImpl<X> bequest(ProperTypeConstructorMixin<X, ?, ?> newOwner) {
        throw null;
    }

    @Override
    public String getName() {
        return name;
    }

    public PropertyImpl<X> substitute(ProperTypeMixin<X, ?> newOwner, Substitutions<X> substitutions) {
        return this;
    }

    @Override
    public de.benshu.cofi.types.Property unbind() {
        return new Unbound<>(this);
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundMember<X, PropertyImpl<X>>
            implements de.benshu.cofi.types.Property {

        public Unbound(PropertyImpl<X> unbound) {
            super(unbound);
        }
    }
}