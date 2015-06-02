package de.benshu.cofi.types.impl.constraints;

import com.google.common.collect.ImmutableSet;

import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeVariableImpl;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;

import java.util.stream.Stream;

public abstract class Monosemous<X extends TypeSystemContext<X>> extends AbstractConstraints<X> {
    public abstract Monosemous<X> getParent();

    public Conjunction<X> and(X context, TypeVariableImpl<X, ?> var, Constraint<X> c) {
        throw new AssertionError();
    }

    public boolean includesUpperBound(X context, TypeVariableImpl<X, ?> variable, TypeMixin<X, ?> bound) {
        throw new AssertionError();
    }

    public boolean includesLowerBound(X context, TypeVariableImpl<X, ?> variable, TypeMixin<X, ?> bound) {
        throw new AssertionError();
    }

    public abstract TypeMixin<X, ?> getUpperBound(X context, TypeVariableImpl<X, ?> typeVariable);

    public abstract TypeMixin<X, ?> getLowerBound(X context, TypeVariableImpl<X, ?> typeVariable);

    public abstract ImmutableSet<Constraint<X>> getConstraints(TypeVariableImpl<X, ?> var);

    public AbstractTypeList<X, TemplateTypeImpl<X>> getMetas(TypeVariableImpl<X, ?> typeVariable) {
        return getBounds(typeVariable, Constraint.Kind.META, TemplateTypeImpl.class);
    }

    public AbstractTypeList<X, ProperTypeMixin<X, ?>> getUppers(TypeVariableImpl<X, ?> typeVariable) {
        return getBounds(typeVariable, Constraint.Kind.UPPER, ProperTypeMixin.class);
    }

    public AbstractTypeList<X, ProperTypeMixin<X, ?>> getLowers(TypeVariableImpl<X, ?> typeVariable) {
        return getBounds(typeVariable, Constraint.Kind.LOWER, ProperTypeMixin.class);
    }

    private <T extends TypeMixin<X, ?>, S extends T> AbstractTypeList<X, T> getBounds(TypeVariableImpl<X, ?> variable, Constraint.Kind kind, Class<S> klazz) {
        Stream<? extends T> types = getConstraints(variable).stream()
                .filter(c -> c.getKind() == kind)
                .map(c -> klazz.cast(c.getBound()));
        return types.collect(AbstractTypeList.<X, T>typeList());
    }
}
