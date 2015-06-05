package de.benshu.cofi.types.impl.templates;

import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.types.ProperTypeSort;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.bound.ProperTypeVisitor;
import de.benshu.cofi.types.bound.TemplateType;
import de.benshu.cofi.types.impl.AbstractConstructedType;
import de.benshu.cofi.types.impl.AbstractUnboundConstructedType;
import de.benshu.cofi.types.impl.Bottom;
import de.benshu.cofi.types.impl.Substitutable;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeConstructorInvocation;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeVariableImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.constraints.Monosemous;
import de.benshu.cofi.types.impl.declarations.InterpretedMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.InterpretedMemberDescriptors;
import de.benshu.cofi.types.impl.interpreters.MemberDescriptorsInterpreter;
import de.benshu.cofi.types.impl.intersections.AbstractIntersectionType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.impl.unions.AbstractUnionType;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.Tags;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class TemplateTypeImpl<X extends TypeSystemContext<X>>
        extends AbstractConstructedType<X, TemplateTypeImpl<X>, TemplateTypeConstructorMixin<X>>
        implements Substitutable<X, TemplateTypeImpl<X>>,
                   TemplateType<X, TemplateTypeImpl<X>, TemplateTypeConstructorMixin<X>> {

    private final Tags tags;

    protected TemplateTypeImpl(AbstractTemplateTypeConstructor<X> constructor, AbstractTypeList<X, ?> arguments, Tagger tagger) {
        super(constructor, arguments);

        this.tags = tagger.tag(this);
    }

    @Override
    public AbstractTemplateTypeConstructor<X> getConstructor() {
        return (AbstractTemplateTypeConstructor<X>) super.getConstructor();
    }

    @Override
    public TemplateTypeImpl<X> substitute(Substitutions<X> substitutions) {
        return super.substitute(substitutions);
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public TemplateTypeImpl<X> setTags(IndividualTags tags) {
        return new Derived<>(getOriginal(), getArguments(), TagCombiners.setAll(this, tags));
    }

    abstract Original<X> getOriginal();

    @Override
    public AbstractTypeList<X, TemplateTypeImpl<X>> getSupertypes() {
        return getConstructor().getSupertypes()
                .map(s -> s.apply(getArguments()));
    }

    @Override
    public <T> T accept(ProperTypeVisitor<X, T> visitor) {
        return visitor.visitTemplateType(this);
    }

    @Override
    public ProperTypeSort getSort() {
        return ProperTypeSort.TEMPLATE;
    }

    @Override
    public final AbstractConstraints<X> establishSubtype(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return other.establishSupertype(this, cs);
    }

    @Override
    public final AbstractConstraints<X> establishSubtype(Bottom<X> other, Monosemous<X> cs) {
        return AbstractConstraints.all();
    }

    @Override
    public final AbstractConstraints<X> establishSubtype(AbstractIntersectionType<X, ?> other, Monosemous<X> cs) {
        return other.establishSupertype(this, cs);
    }

    @Override
    public final AbstractConstraints<X> establishSubtype(TypeVariableImpl<X, ?> other, Monosemous<X> cs) {
        return other.establishSupertype(this, cs);
    }

    @Override
    public final AbstractConstraints<X> establishSubtype(AbstractUnionType<X, ?> other, Monosemous<X> cs) {
        return other.establishSupertype(this, cs);
    }

    @Override
    public final AbstractConstraints<X> establishSubtype(TemplateTypeImpl<X> other, Monosemous<X> cs) {
        if (getConstructor().getOriginal().equals(other.getConstructor().getOriginal())) {
            AbstractConstraints<X> result = cs;

            final AbstractTypeList<X, ?> myArgs = getArguments();
            final AbstractTypeList<X, ?> othersArgs = other.getArguments();

            final int I = getConstructor().getParameters().size();
            for (int i = 0; i < I; ++i) {
                final TypeParameterImpl<X> param = getConstructor().getParameters().get(i);

                TypeMixin<X, ?> myArg = myArgs.get(i);
                TypeMixin<X, ?> othersArg = othersArgs.get(i);

                result = establishArgSubtype(param, myArg, othersArg, cs, result);
            }

            return result.or(establishSubtypeGeneric(other, cs));
        } else {
            return establishSubtypeGeneric(other, cs);
        }
    }

    private AbstractConstraints<X> establishArgSubtype(TypeParameterImpl<X> param, TypeMixin<X, ?> myArg, TypeMixin<X, ?> othersArg, Monosemous<X> cs, AbstractConstraints<X> result) {
//        if (othersArg instanceof WildcardTypeImpl || myArg instanceof WildcardTypeImpl) {
//            return othersArg instanceof WildcardTypeImpl ? result : AbstractConstraints.all();
//        }

        final AbstractConstraints<X> sub = myArg.establishSubtype(othersArg, cs);
        final AbstractConstraints<X> sup = myArg.establishSupertype(othersArg, cs);

        switch (param.getVariance()) {
            case CONTRAVARIANT:
                return result.and(sup);
            case COVARIANT:
                return result.and(sub);
            case INVARIANT:
                return result.and(sub).and(sup);
            case BIVARIANT:
                return result;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public final AbstractConstraints<X> establishSubtypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        // FIXME which cases are these? maybe swap with all the "other.establishSupertype(this, cs)" cases.
        AbstractConstraints<X> result = AbstractConstraints.all();

        for (TemplateTypeImpl<X> t : getSupertypes()) {
            result = result.or(t.establishSubtype(other, cs));
        }

        return result;
    }

    @Override
    public final AbstractConstraints<X> establishSupertype(Bottom<X> other, Monosemous<X> cs) {
        return cs;
    }

    @Override
    public AbstractConstraints<X> establishSupertypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return other.establishSubtype(this, cs);
    }

    @Override
    public de.benshu.cofi.types.TemplateType unbind() {
        return new Unbound<>(this);
    }

    @Override
    public Optional<TypeConstructorInvocation<X>> tryGetInvocationOf(TypeConstructorMixin<X, ?, ?> typeConstructor) {
        return super.tryGetInvocationOf(typeConstructor)
                .map(Optional::of)
                .orElseGet(() -> getSupertypes().stream()
                        .map(t -> t.tryGetInvocationOf(typeConstructor))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .reduce(TypeConstructorInvocation::combine));
    }

    static class Original<X extends TypeSystemContext<X>> extends TemplateTypeImpl<X> {
        protected Original(AbstractTemplateTypeConstructor<X> constructor) {
            super(constructor, constructor.getParameters().getVariables(), HashTags::createEmpty);
        }

        @Override
        Original<X> getOriginal() {
            return this;
        }

        public TemplateTypeImpl<X> apply(AbstractTypeList<X, ?> arguments) {
            return new Derived<>(this, arguments, TagCombiners.apply(getConstructor().getTags()));
        }

        @Override
        public ImmutableMap<String, AbstractMember<X>> getMembers() {
            final InterpretedMemberDescriptors<X> interpretedMemberDescriptors = getConstructor().getOriginal().getDeclaration()
                    .supplyMembers(getContext(), MemberDescriptorsInterpreter.create(getContext()));

            final AbstractConstraints<X> contextualConstraints = interpretedMemberDescriptors.getContext();

            final Set<String> names = new HashSet<>();
            final ImmutableMap.Builder<String, AbstractMember<X>> builder = ImmutableMap.builder();

            for (InterpretedMemberDescriptor<X> desc : interpretedMemberDescriptors.getDescriptors()) {
                final String name = desc.getName();

                final AbstractMember<X> sm = getSuperMember(contextualConstraints, name);
                builder.put(name, sm == null
                        ? AbstractMember.create(getContext(), Original.this, desc)
                        : sm.refine(contextualConstraints, desc));
                names.add(name);
            }

            for (TemplateTypeImpl<X> st : getSupertypes()) {
                for (String name : st.getMembers().keySet()) {
                    if (!names.contains(name)) {
                        builder.put(name, getSuperMember(contextualConstraints, name));
                        names.add(name);
                    }
                }
            }

            return builder.build();
        }

        private AbstractMember<X> getSuperMember(AbstractConstraints<X> contextualConstraints, String name) {
            AbstractMember<X> sm = null;

            for (TemplateTypeImpl<X> st : getSupertypes()) {
                for (AbstractMember<X> stm : st.lookupMember(name)) {
                    final AbstractMember<X> bequeathed = stm.bequest(getConstructor());
                    sm = sm == null ? bequeathed : sm.intersectWith(contextualConstraints, bequeathed);
                }
            }

            return sm == null ? null : sm;
        }
    }

    static class Derived<X extends TypeSystemContext<X>> extends TemplateTypeImpl<X> {
        private final Original<X> original;

        public Derived(Original<X> original, AbstractTypeList<X, ?> arguments, Tagger tagger) {
            super(original.getConstructor(), arguments, tagger);

            this.original = original;
        }

        @Override
        Original<X> getOriginal() {
            return original;
        }

        @Override
        public ImmutableMap<String, AbstractMember<X>> getMembers() {
            Substitutions<X> substitutions = Substitutions.ofThrough(getConstructor().getParameters(), getArguments());
            final ImmutableMap.Builder<String, AbstractMember<X>> builder = ImmutableMap.builder();
            for (Map.Entry<String, AbstractMember<X>> entry : getOriginal().getMembers().entrySet()) {
                builder.put(entry.getKey(), entry.getValue().substitute(this, substitutions));
            }
            return builder.build();
        }
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundConstructedType<X, TemplateTypeImpl<X>, TemplateTypeConstructorMixin<X>, TemplateTypeConstructor>
            implements de.benshu.cofi.types.TemplateType {

        public Unbound(TemplateTypeImpl<X> unbound) {
            super(unbound);
        }

        @Override
        public TypeList<? extends de.benshu.cofi.types.TemplateType> getSupertypes() {
            return bound.getSupertypes().unbind();
        }

        @Override
        public TemplateTypeConstructor getConstructor() {
            return bound.getConstructor().unbind();
        }

        @Override
        public <T> T accept(de.benshu.cofi.types.ProperTypeVisitor<T> visitor) {
            return visitor.visitTemplateType(this);
        }
    }
}
