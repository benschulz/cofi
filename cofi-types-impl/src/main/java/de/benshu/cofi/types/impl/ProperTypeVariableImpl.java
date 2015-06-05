package de.benshu.cofi.types.impl;

import com.google.common.collect.ImmutableMap;

import de.benshu.cofi.types.ProperTypeSort;
import de.benshu.cofi.types.TypeParameter;
import de.benshu.cofi.types.TypeParameterList;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.constraints.Constraint;
import de.benshu.cofi.types.impl.constraints.Monosemous;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.bound.ProperTypeVariable;
import de.benshu.cofi.types.bound.ProperTypeVisitor;
import de.benshu.commons.core.Optional;
import static de.benshu.cofi.types.impl.constraints.AbstractConstraints.all;

public class ProperTypeVariableImpl<X extends TypeSystemContext<X>>
        extends TypeVariableImpl<X, ProperTypeVariableImpl<X>>
        implements ProperTypeMixin<X, ProperTypeVariableImpl<X>>,
                   ProperTypeVariable<X, ProperTypeVariableImpl<X>>,
                   Substitutable<X, ProperTypeMixin<X, ?>> {

    public static <X extends TypeSystemContext<X>> ProperTypeVariableImpl<X> create(TypeParameterListImpl<X> parameterList, TypeParameterImpl<X> parameter) {
        return new ProperTypeVariableImpl<>(parameterList, parameter, HashTags::createEmpty);
    }

    private ProperTypeVariableImpl(TypeParameterListImpl<X> parameterList, TypeParameterImpl<X> parameter, Tagger tagger) {
        super(parameterList, parameter, tagger);
    }

    @Override
    public AbstractConstraints<X> establishSubtype(TypeMixin<X, ?> other, Monosemous<X> constraints) {
        return other.establishSupertype(this, constraints);
    }

    @Override
    public AbstractConstraints<X> establishSubtypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        if (equals(other))
            return cs;
        else if (cs.includesUpperBound(this, other))
            return cs;
        else if (cs.getTypeParams().contains(this))
            return cs.and(getContext(), this, Constraint.upper(other));
        else
            return all();
    }

    @Override
    public AbstractConstraints<X> establishSupertype(TypeMixin<X, ?> other, Monosemous<X> constraints) {
        return other.establishSubtype(this, constraints);
    }

    @Override
    public AbstractConstraints<X> establishSupertypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        if (equals(other))
            return cs;
        else if (cs.includesLowerBound(this, other))
            return cs;
        else if (cs.getTypeParams().contains(this))
            return cs.and(getContext(), this, Constraint.lower(other));
        else
            return all();
    }

    @Override
    public ProperTypeVariableImpl<X> setTags(IndividualTags tags) {
        return new ProperTypeVariableImpl<>(getParameterList(), getParameter(), TagCombiners.setAll(this, tags));
    }

    @Override
    public <T> T accept(ProperTypeVisitor<X, T> visitor) {
        return visitor.visitTypeVariable(this);
    }

    @Override
    public ProperTypeSort getSort() {
        return ProperTypeSort.VARIABLE;
    }

    @Override
    public ProperTypeMixin<X, ?> substitute(Substitutions<X> substitutions) {
        return (ProperTypeMixin<X, ?>) super.substitute(substitutions);
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
    public java.util.Optional<TypeConstructorInvocation<X>> tryGetInvocationOf(TypeConstructorMixin<X, ?, ?> typeConstructor) {
        return java.util.Optional.empty();
    }

    @Override
    public Unbound<X> unbind() {
        return new Unbound<>(this);
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundProperType<X, ProperTypeVariableImpl<X>>
            implements de.benshu.cofi.types.ProperTypeVariable {

        public Unbound(ProperTypeVariableImpl<X> unbound) {
            super(unbound);
        }

        @Override
        public <T> T accept(de.benshu.cofi.types.ProperTypeVisitor<T> visitor) {
            return visitor.visitTypeVariable(this);
        }

        @Override
        public TypeParameterList getParameterList() {
            return bound.getParameterList().unbind();
        }

        @Override
        public TypeParameter getParameter() {
            return bound.getParameter().unbind();
        }
    }
}
