package de.benshu.cofi.types.impl.members;

import com.google.common.collect.ImmutableSet;

import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.bound.Method;
import de.benshu.cofi.types.impl.AdHoc;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.intersections.AnonymousIntersectionType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.impl.tags.TaggedMixin;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import de.benshu.cofi.types.tags.Tags;

import java.util.stream.Stream;

import static de.benshu.cofi.types.impl.lists.AbstractTypeList.typeList;
import static de.benshu.commons.core.streams.Collectors.list;
import static java.util.stream.Collectors.joining;

public final class MethodSignatureImpl<X extends TypeSystemContext<X>> implements TaggedMixin, Method.Signature<X> {
    public final int index;
    public final TypeParameterListImpl<X> typeParameters;
    public final AbstractTypeList<X, ProperTypeMixin<X, ?>> parameterTypes;
    public final ProperTypeMixin<X, ?> returnType;
    public final ImmutableSet<MethodSignatureImpl<X>> supersignatures;
    public final Tags tags;

    MethodSignatureImpl(int index, TypeParameterListImpl<X> typeParameters, AbstractTypeList<X, ProperTypeMixin<X, ?>> parameterTypes, ProperTypeMixin<X, ?> returnType, ImmutableSet<MethodSignatureImpl<X>> supersignatures, Tagger tagger) {
        this.index = index;
        this.typeParameters = typeParameters;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.supersignatures = supersignatures;
        this.tags = tagger.tag(this);
    }

    public MethodSignatureImpl<X> bequest(ProperTypeConstructorMixin<X, ?, ?> newOwner, Substitutions<X> substitutions) {
        return new MethodSignatureImpl<>(
                index,
                typeParameters.getConstraints().substitute(returnType.getContext(), newOwner.getParameters(), substitutions).getTypeParams(),
                parameterTypes,
                returnType,
                supersignatures,
                TagCombiners.bequest(getTags())
        );
    }

    @Override
    public TemplateTypeConstructorMixin<X> getType() {
        final X context = returnType.getContext();

        final AbstractTypeList<X, ProperTypeMixin<X, ?>> returnTypes = Stream.<ProperTypeMixin<X, ?>>concat(
                Stream.of(returnType),
                supersignatures.stream()
                        .map(s -> s.returnType)
        ).collect(typeList());


        final TypeMixin<X, ?> out = AnonymousIntersectionType.createIfNonTrivial(context, returnTypes);

        return AdHoc.templateTypeConstructor(context, typeParameters, context.getTypeSystem().constructFunction(parameterTypes, out));
    }

    @Override
    public String toString() {
        return "[" + index + "] " + parameterTypes.stream().map(Object::toString).collect(list()).stream().collect(joining(", ")) + " -> " + returnType;
    }

    public Unbound<X> unbind() {
        return new Unbound<>(this);
    }

    public Tags getTags() {
        return tags;
    }

    static class Unbound<X extends TypeSystemContext<X>> implements de.benshu.cofi.types.Method.Signature {
        private final MethodSignatureImpl<X> unbound;

        public Unbound(MethodSignatureImpl<X> unbound) {
            this.unbound = unbound;
        }

        @Override
        public TemplateTypeConstructor getType() {
            return unbound.getType().unbind();
        }
    }
}