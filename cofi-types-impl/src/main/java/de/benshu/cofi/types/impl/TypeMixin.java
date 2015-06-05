package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.bound.Type;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.constraints.Monosemous;
import de.benshu.cofi.types.impl.intersections.AbstractIntersectionType;
import de.benshu.cofi.types.impl.tags.TaggedMixin;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.unions.AbstractUnionType;

import java.util.Collection;

public interface TypeMixin<X extends TypeSystemContext<X>, S extends TypeMixin<X, S>>
        extends Type<X, S>, TaggedMixin {

    static <X extends TypeSystemContext<X>> TypeMixin<X, ?> rebind(de.benshu.cofi.types.Type unbound) {
        return ((AbstractUnboundType<?, ?>) unbound).<X>rebind().bound;
    }

    @SuppressWarnings("unchecked")
    default S self() {
        return (S) this;
    }

    boolean isSameAs(TypeMixin<X, ?> other);

    TypeMixin<X, ?> substitute(Substitutions<X> substitutions);

    default AbstractConstraints<X> establishSubtype(Error<X> other, Monosemous<X> cs) {
        return cs;
    }

    default AbstractConstraints<X> establishSubtype(Bottom<X> other, Monosemous<X> cs) {
        return establishSubtypeGeneric(other, cs);
    }

    default AbstractConstraints<X> establishSubtype(AbstractIntersectionType<X, ?> other, Monosemous<X> cs) {
        return other.establishSupertype(this, cs);
    }

    default AbstractConstraints<X> establishSubtype(TemplateTypeImpl<X> other, Monosemous<X> cs) {
        return establishSubtypeGeneric(other, cs);
    }

    default AbstractConstraints<X> establishSubtype(TypeVariableImpl<X, ?> other, Monosemous<X> cs) {
        return establishSubtypeGeneric(other, cs);
    }

    default AbstractConstraints<X> establishSubtype(AbstractUnionType<X, ?> other, Monosemous<X> cs) {
        return establishSubtypeGeneric(other, cs);
    }

    default AbstractConstraints<X> establishSubtype(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return other.establishSupertype(this, cs);
    }

    default AbstractConstraints<X> establishSubtypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        throw new AssertionError();
    }

    default AbstractConstraints<X> establishSupertype(Error<X> other, Monosemous<X> cs) {
        return cs;
    }

    default AbstractConstraints<X> establishSupertype(Bottom<X> other, Monosemous<X> cs) {
        return establishSupertypeGeneric(other, cs);
    }

    default AbstractConstraints<X> establishSupertype(AbstractIntersectionType<X, ?> other, Monosemous<X> cs) {
        return establishSupertypeGeneric(other, cs);
    }

    default AbstractConstraints<X> establishSupertype(TemplateTypeImpl<X> other, Monosemous<X> cs) {
        return establishSupertypeGeneric(other, cs);
    }

    default AbstractConstraints<X> establishSupertype(TypeVariableImpl<X, ?> other, Monosemous<X> cs) {
        return establishSupertypeGeneric(other, cs);
    }

    default AbstractConstraints<X> establishSupertype(AbstractUnionType<X, ?> other, Monosemous<X> cs) {
        return establishSupertypeGeneric(other, cs);
    }

    default AbstractConstraints<X> establishSupertype(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return establishSupertypeGeneric(other, cs);
    }

    default AbstractConstraints<X> establishSupertypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        throw new AssertionError();
    }

    default boolean containsAny(Collection<TypeVariableImpl<X, ?>> variables) {
        return false;
    }

    de.benshu.cofi.types.Type unbind();

    // TODO Try and get rid of this method…
    default X getContext() {
        throw new AssertionError();
    }

    String toDescriptor();
}
