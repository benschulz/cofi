package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.TemplateType;
import de.benshu.cofi.types.Type;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.Tags;
import static com.google.common.base.Preconditions.checkArgument;

public class AdHoc {
    public static <X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> GenericTypeConstructor<X, T> typeConstructor(X context, TypeParameterListImpl<X> parameters, Substitutable<X, ? extends T> unconstructed) {
        return new GenericTypeConstructor<>(context, parameters, unconstructed, HashTags::createEmpty);
    }

    public static <X extends TypeSystemContext<X>, T extends ProperTypeMixin<X, ?>> ProperTypeConstructor<X, T> properTypeConstructor(X context, TypeParameterListImpl<X> parameters, Substitutable<X, ? extends T> unconstructed) {
        return new ProperTypeConstructor<>(context, parameters, unconstructed, HashTags::createEmpty);
    }

    public static <X extends TypeSystemContext<X>, T extends TemplateTypeImpl<X>> TemplateTypeConstructor<X> templateTypeConstructor(X context, TypeParameterListImpl<X> parameters, Substitutable<X, ? extends T> unconstructed) {
        return new TemplateTypeConstructor<>(context, parameters, unconstructed, HashTags::createEmpty);
    }

    private static class GenericTypeConstructor<X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> extends TypeConstructor<X, GenericTypeConstructor<X, T>, T> {
        private GenericTypeConstructor(X context, TypeParameterListImpl<X> typeParameters, Substitutable<X, ? extends T> unconstructed, Tagger tagger) {
            super(context, typeParameters, unconstructed, tagger);
        }

        @Override
        public GenericTypeConstructor<X, T> setTags(IndividualTags tags) {
            return new GenericTypeConstructor<>(getContext(), parameters, unconstructed, TagCombiners.setAll(this, tags));
        }

        @Override
        public Unbound<X> unbind() {
            return new Unbound<>(this);
        }

        private static class Unbound<X extends TypeSystemContext<X>>
                extends AbstractUnboundTypeConstructor<X, TypeConstructor<X, ?, ?>, Type> {

            public Unbound(TypeConstructor<X, ?, ?> unbound) {
                super(unbound);
            }

            @Override
            public Type apply(TypeList<?> arguments) {
                return bound.apply(AbstractTypeList.rebind(arguments)).unbind();
            }
        }
    }

    private static class ProperTypeConstructor<X extends TypeSystemContext<X>, T extends ProperTypeMixin<X, ?>>
            extends TypeConstructor<X, ProperTypeConstructor<X, T>, T>
            implements ProperTypeConstructorMixin<X, ProperTypeConstructor<X, T>, T> {

        private ProperTypeConstructor(X context, TypeParameterListImpl<X> typeParameters, Substitutable<X, ? extends T> unconstructed, Tagger tagger) {
            super(context, typeParameters, unconstructed, tagger);
        }

        @Override
        public ProperTypeConstructor<X, T> setTags(IndividualTags tags) {
            return new ProperTypeConstructor<>(getContext(), parameters, unconstructed, TagCombiners.setAll(this, tags));
        }

        @Override
        public Unbound<X> unbind() {
            return new Unbound<>(this);
        }

        private static class Unbound<X extends TypeSystemContext<X>>
                extends AbstractUnboundProperTypeConstructor<X, ProperTypeConstructor<X, ?>, ProperType> {

            public Unbound(ProperTypeConstructor<X, ?> unbound) {
                super(unbound);
            }

            @Override
            public ProperType apply(TypeList<?> arguments) {
                return bound.apply(AbstractTypeList.rebind(arguments)).unbind();
            }
        }
    }

    public static class TemplateTypeConstructor<X extends TypeSystemContext<X>>
            extends TypeConstructor<X, TemplateTypeConstructorMixin<X>, TemplateTypeImpl<X>>
            implements TemplateTypeConstructorMixin<X> {

        private TemplateTypeConstructor(X context, TypeParameterListImpl<X> typeParameters, Substitutable<X, ? extends TemplateTypeImpl<X>> unconstructed, Tagger tagger) {
            super(context, typeParameters, unconstructed, tagger);
        }

        @Override
        public TemplateTypeConstructorMixin<X> setTags(IndividualTags tags) {
            return new TemplateTypeConstructor<>(getContext(), parameters, unconstructed, TagCombiners.setAll(this, tags));
        }

        @Override
        public AbstractTypeList<X, TemplateTypeConstructorMixin<X>> getSupertypes() {
            return unconstructed.substitute(Substitutions.trivialOf(getParameters())).getSupertypes()
                    .map(s -> templateTypeConstructor(getContext(), getParameters(), s));
        }

        @Override
        public Unbound<X> unbind() {
            return new Unbound<>(this);
        }

        public static class Unbound<X extends TypeSystemContext<X>>
                extends AbstractUnboundProperTypeConstructor<X, TemplateTypeConstructor<X>, TemplateType>
                implements de.benshu.cofi.types.TemplateTypeConstructor {

            public Unbound(TemplateTypeConstructor<X> unbound) {
                super(unbound);
            }

            @Override
            public TypeList<de.benshu.cofi.types.TemplateTypeConstructor> getSupertypes() {
                return bound.getSupertypes().unbind();
            }

            @Override
            public TemplateType apply(TypeList<?> arguments) {
                return bound.apply(AbstractTypeList.rebind(arguments)).unbind();
            }
        }
    }

    public static abstract class TypeConstructor<X extends TypeSystemContext<X>, S extends TypeConstructorMixin<X, S, T>, T extends TypeMixin<X, ?>>
            extends AbstractTypeConstructor<X, S, T> {

        final TypeParameterListImpl<X> parameters;
        final Substitutable<X, ? extends T> unconstructed;
        final Tags tags;

        private TypeConstructor(X context, TypeParameterListImpl<X> parameters, Substitutable<X, ? extends T> unconstructed, Tagger tagger) {
            super(context);

            this.parameters = parameters;
            this.unconstructed = unconstructed;
            this.tags = tagger.tag(this);
        }

        @Override
        public Tags getTags() {
            return tags;
        }

        @Override
        public T apply(AbstractTypeList<X, ?> arguments) {
            checkArgument(arguments.size() == getParameters().size());

            return unconstructed.substitute(Substitutions.ofThrough(parameters, arguments));
        }

        @Override
        public TypeParameterListImpl<X> getParameters() {
            return parameters;
        }

        @Override
        public TypeConstructorMixin<X, ?, ?> substitute(Substitutions<X> substitutions) {
            checkArgument(getParameters().getVariables().stream().noneMatch(substitutions::substitutes));

            if (true)
                throw null; // TODO parameter constraints must be substituted too...

            Substitutable<X, ? extends T> substituted = (Substitutable<X, ? extends T>) unconstructed.substitute(substitutions);
            return substituted == unconstructed ? this : typeConstructor(getContext(), parameters, substituted);
        }

        @Override
        public String toDescriptor() {
            return unconstructed.toDescriptor();
        }
    }
}

