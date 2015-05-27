package de.benshu.cofi.types.impl.constraints;

import de.benshu.cofi.types.impl.*;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;

public abstract class Constraint<X extends TypeSystemContext<X>> {
    public enum Kind {
        LOWER,
        META,
        UPPER,;
    }

    private static final class Lower<X extends TypeSystemContext<X>> extends Constraint<X> {
        public Lower(TypeMixin<X, ?> bound) {
            super(bound);
        }

        @Override
        public Kind getKind() {
            return Kind.LOWER;
        }

        @Override
        public boolean implies(X context, Constraint<X> other, Conjunction<X> cs) {
            return other.isImplied(context, this, cs);
        }

        @Override
        boolean implies(X context, Lower<X> other, Conjunction<X> cs) {
            return getBound().establishSupertype(other.getBound(), cs).contains(cs);
        }

        @Override
        boolean implies(X context, Meta<X> other, Conjunction<X> cs) {
            return false;
        }

        @Override
        boolean implies(X context, Upper<X> other, Conjunction<X> cs) {
            return false;
        }

        @Override
        boolean isImplied(X context, Constraint<X> other, Conjunction<X> cs) {
            return other.implies(context, this, cs);
        }

        @Override
        public AbstractConstraints<X> reconcile(X context, Constraint<X> other, Conjunction<X> cs) {
            return other.reconcile(context, this, cs);
        }

        @Override
        AbstractConstraints<X> reconcile(X context, Lower<X> other, Conjunction<X> cs) {
            return cs;
        }

        @Override
        AbstractConstraints<X> reconcile(X context, Meta<X> other, Conjunction<X> cs) {
            return cs;
        }

        @Override
        AbstractConstraints<X> reconcile(X context, Upper<X> other, Conjunction<X> cs) {
            return other.reconcile(context, this, cs);
        }

        @Override
        public String debug(String parameter) {
            return getBound() + " <: " + parameter;
        }

        @Override
        public boolean check(X context, AbstractConstraints<X> cs, Substitutions<X> substitutions, TypeVariableImpl<X, ?> variable) {
            return cs.isSubtype(getBound().substitute(substitutions), substitutions.substitute(variable));
        }

        @Override
        Constraint<X> substitute(X context, Substitutions<X> substitutions) {
            return new Lower<>(getBound().substitute(substitutions));
        }
    }

    private static class Meta<X extends TypeSystemContext<X>> extends Constraint<X> {
        public Meta(TemplateTypeImpl<X> bound) {
            super(bound);
        }

        @Override
        public TemplateTypeImpl<X> getBound() {
            return (TemplateTypeImpl<X>) super.getBound();
        }

        @Override
        public Kind getKind() {
            return Kind.META;
        }

        @Override
        public boolean implies(X context, Constraint<X> other, Conjunction<X> cs) {
            return other.isImplied(context, this, cs);
        }

        @Override
        boolean implies(X context, Lower<X> other, Conjunction<X> cs) {
            return false;
        }

        @Override
        boolean implies(X context, Meta<X> other, Conjunction<X> cs) {
            return getBound().establishSubtype(other.getBound(), cs).contains(cs);
        }

        @Override
        boolean implies(X context, Upper<X> other, Conjunction<X> cs) {
            return false;
        }

        @Override
        boolean isImplied(X context, Constraint<X> other, Conjunction<X> cs) {
            return other.implies(context, this, cs);
        }

        @Override
        public AbstractConstraints<X> reconcile(X context, Constraint<X> other, Conjunction<X> cs) {
            return other.reconcile(context, this, cs);
        }

        @Override
        AbstractConstraints<X> reconcile(X context, Lower<X> other, Conjunction<X> cs) {
            return cs;
        }

        @Override
        AbstractConstraints<X> reconcile(X context, Meta<X> other, Conjunction<X> cs) {
            // TODO same as for Upper.reconcile(Upper, Concunction)
            return cs;
        }

        @Override
        AbstractConstraints<X> reconcile(X context, Upper<X> other, Conjunction<X> cs) {
            return cs;
        }

        @Override
        public String debug(String parameter) {
            return parameter + " : " + getBound();
        }

        @Override
        public boolean check(X context, AbstractConstraints<X> cs, Substitutions<X> substitutions, TypeVariableImpl<X, ?> variable) {
            throw null;
        }

        @Override
        Constraint<X> substitute(X context, Substitutions<X> substitutions) {
            return new Meta<>(getBound().substitute(substitutions));
        }
    }

    private static final class Upper<X extends TypeSystemContext<X>> extends Constraint<X> {
        public Upper(TypeMixin<X, ?> bound) {
            super(bound);
        }

        @Override
        public Kind getKind() {
            return Kind.UPPER;
        }

        @Override
        public boolean implies(X context, Constraint<X> other, Conjunction<X> cs) {
            return other.isImplied(context, this, cs);
        }

        @Override
        boolean implies(X context, Lower<X> other, Conjunction<X> cs) {
            return false;
        }

        @Override
        boolean implies(X context, Meta<X> other, Conjunction<X> cs) {
            return false;
        }

        @Override
        boolean implies(X context, Upper<X> other, Conjunction<X> cs) {
            return getBound().establishSubtype(other.getBound(), cs).contains(cs);
        }

        @Override
        boolean isImplied(X context, Constraint<X> other, Conjunction<X> cs) {
            return other.implies(context, this, cs);
        }

        @Override
        public AbstractConstraints<X> reconcile(X context, Constraint<X> other, Conjunction<X> cs) {
            return other.reconcile(context, this, cs);
        }

        @Override
        AbstractConstraints<X> reconcile(X context, Lower<X> other, Conjunction<X> cs) {
            return other.getBound().establishSubtype(getBound(), cs);
        }

        @Override
        AbstractConstraints<X> reconcile(X context, Meta<X> other, Conjunction<X> cs) {
            return cs;
        }

        @Override
        AbstractConstraints<X> reconcile(X context, Upper<X> other, Conjunction<X> cs) {
            // TODO the base classes must be compatible

            // TODO if this bound contains a supertype of the form G_a<T_{a,1}, ..., T_{a,n}>
            // and the other bound contains a supertype of the form G_b<T_{b,1}, ..., T_{b,n}>
            // and G_a = G = G_b, then T_{a,i} = T_{b,i} must be established for every invariant
            // type parameter P_i of G.

            //			final TypeImpl myType = getBound().substitute(substitutions);
            //			final TypeImpl otherType = other.getBound().substitute(substitutions);
            //
            //			return myType.isSubtype(otherType).or(otherType.isSubtype(myType));
            return cs;
        }

        @Override
        public String debug(String parameter) {
            return parameter + " <: " + getBound();
        }

        @Override
        public boolean check(X context, AbstractConstraints<X> cs, Substitutions<X> substitutions, TypeVariableImpl<X, ?> variable) {
            return cs.isSubtype(substitutions.substitute(variable), getBound().substitute(substitutions));
        }

        @Override
        Constraint<X> substitute(X context, Substitutions<X> substitutions) {
            return new Upper<>(getBound().substitute(substitutions));
        }
    }

    public static <X extends TypeSystemContext<X>> Constraint<X> lower(TypeMixin<X, ?> bound) {
        return new Lower<>(bound);
    }

    public static <X extends TypeSystemContext<X>> Constraint<X> upper(TypeMixin<X, ?> bound) {
        return new Upper<>(bound);
    }

    private final TypeMixin<X, ?> bound;

    private Constraint(TypeMixin<X, ?> bound) {
        this.bound = bound;
    }

    public TypeMixin<X, ?> getBound() {
        return bound;
    }

    public abstract Kind getKind();

    public abstract boolean implies(X context, Constraint<X> other, Conjunction<X> cs);

    abstract boolean implies(X context, Lower<X> other, Conjunction<X> cs);

    abstract boolean implies(X context, Meta<X> other, Conjunction<X> cs);

    abstract boolean implies(X context, Upper<X> other, Conjunction<X> cs);

    abstract boolean isImplied(X context, Constraint<X> other, Conjunction<X> cs);

    public abstract AbstractConstraints<X> reconcile(X context, Constraint<X> other, Conjunction<X> cs);

    abstract AbstractConstraints<X> reconcile(X context, Lower<X> other, Conjunction<X> cs);

    abstract AbstractConstraints<X> reconcile(X context, Meta<X> other, Conjunction<X> cs);

    abstract AbstractConstraints<X> reconcile(X context, Upper<X> other, Conjunction<X> cs);

    @Override
    public final String toString() {
        return debug("?");
    }

    abstract String debug(String parameter);

    public String toString(TypeVariableImpl<X, ?> var) {
        return debug(var.debug());
    }

    public abstract boolean check(X context, AbstractConstraints<X> cs, Substitutions<X> substitutions, TypeVariableImpl<X, ?> variable);

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof Constraint)) {
            return false;
        }

        final Constraint<X> o = (Constraint<X>) other;
        return getKind() == o.getKind() && getBound().equals(o.getBound());
    }

    @Override
    public int hashCode() {
        return getKind().hashCode() ^ getBound().hashCode();
    }

    abstract Constraint<X> substitute(X context, Substitutions<X> substitutions);
}
