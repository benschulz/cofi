package de.benshu.cofi.types.impl;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.benshu.cofi.types.Kind;
import de.benshu.cofi.types.bound.Bottom;
import de.benshu.cofi.types.bound.ErrorType;
import de.benshu.cofi.types.bound.IntersectionType;
import de.benshu.cofi.types.bound.ProperType;
import de.benshu.cofi.types.bound.ProperTypeVariable;
import de.benshu.cofi.types.bound.ProperTypeVisitor;
import de.benshu.cofi.types.bound.TemplateType;
import de.benshu.cofi.types.bound.UnionType;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.intersections.AbstractIntersectionType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.unions.AbstractUnionType;
import de.benshu.commons.core.Optional;

public interface ProperTypeMixin<X extends TypeSystemContext<X>, S extends ProperTypeMixin<X, S>>
        extends TypeMixin<X, S>, ProperType<X, S> {

    @Override
    ProperTypeMixin<X, ?> substitute(Substitutions<X> substitutions);

    default Substitutable<X, ? extends ProperTypeMixin<X, ?>> substitutable() {
        return new Substitutable<X, ProperTypeMixin<X, ?>>() {
            @Override
            public ProperTypeMixin<X, ?> substitute(Substitutions<X> substitutions) {
                return ProperTypeMixin.this;
            }

            @Override
            public String toDescriptor() {
                return ProperTypeMixin.this.toDescriptor();
            }
        };
    }

    @Override
    default Optional<AbstractMember<X>> lookupMember(String name) {
        return Optional.from(getMembers().get(name));
    }

    @Override
    default Kind getKind() {
        return KindImpl.PROPER_ORDER;
    }

    ImmutableMap<String, AbstractMember<X>> getMembers();

    default AbstractConstraints<X> letContext() {
        ImmutableList<AbstractConstraints<X>> constraints = accept(new ProperTypeVisitor<X, ImmutableList<AbstractConstraints<X>>>() {
            @Override
            public ImmutableList<AbstractConstraints<X>> visitBottomType(Bottom<X, ?> bottomType) {
                return ImmutableList.of();
            }

            @Override
            public ImmutableList<AbstractConstraints<X>> visitIntersectionType(IntersectionType<X, ?> intersectionType) {
                return visitElementsOrArgs(((AbstractIntersectionType<X, ?>) intersectionType).getElements());
            }

            @Override
            public ImmutableList<AbstractConstraints<X>> visitUnionType(UnionType<X, ?> unionType) {
                return visitElementsOrArgs(((AbstractUnionType<X, ?>) unionType).getElements());
            }

            @Override
            public ImmutableList<AbstractConstraints<X>> visitTemplateType(TemplateType<X, ?, ?> templateType) {
                return visitElementsOrArgs(((TemplateTypeImpl<X>) templateType).getArguments());
            }

            private ImmutableList<AbstractConstraints<X>> visitElementsOrArgs(AbstractTypeList<X, ?> elementsOrArgs) {
                return FluentIterable.from(elementsOrArgs).transformAndConcat(t -> ((ProperTypeMixin<X, ?>) t).accept(this)).toList();
            }

            @Override
            public ImmutableList<AbstractConstraints<X>> visitTypeVariable(ProperTypeVariable<X, ?> properTypeVariable) {
                return ImmutableList.of(((TypeVariableImpl<X, ?>) properTypeVariable).getParameterList().getConstraints());
            }

            @Override
            public ImmutableList<AbstractConstraints<X>> visitErrorType(ErrorType<X, ?> errorType) {
                return ImmutableList.of();
            }
        });

        AbstractConstraints<X> contextualConstraints = AbstractConstraints.none();
        // TODO filter dupes
        for (AbstractConstraints<X> cs : constraints) {
            contextualConstraints = contextualConstraints.append(cs);
        }
        return contextualConstraints;
    }

    @Override
    de.benshu.cofi.types.ProperType unbind();
}
