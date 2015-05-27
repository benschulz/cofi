package de.benshu.cofi.types.impl.constraints;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import de.benshu.cofi.types.bound.Type;
import de.benshu.cofi.types.bound.TypeList;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeVariableImpl;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

import static de.benshu.commons.core.streams.Collectors.set;

public class Disjunction<X extends TypeSystemContext<X>> extends AbstractConstraints<X> {
    private final ImmutableList<Monosemous<X>> options;

    public Disjunction(ImmutableList<Monosemous<X>> options) {
        this.options = options;
    }

    @Override
    public AbstractConstraints<X> and(AbstractConstraints<X> constraints) {
        if (constraints.isNone()) {
            return this;
        } else if (constraints.isAll()) {
            return all();
        } else {
            AbstractConstraints<X> result = all();

            for (Monosemous<X> option : options) {
                result = result.or(option.and(constraints));
            }

            return result;
        }
    }

    @Override
    public boolean isAll() {
        return false;
    }

    @Override
    public boolean isDisjunctive() {
        return true;
    }

    @Override
    public boolean isNone() {
        return false;
    }

    @Override
    public AbstractConstraints<X> getParent() {
        final ImmutableSet<Monosemous<X>> parentOptions = options.stream()
                .distinct()
                .map(Monosemous::getParent)
                .collect(set());

        return parentOptions.size() == 1
                ? parentOptions.iterator().next()
                : new Disjunction<>(ImmutableList.copyOf(parentOptions));
    }

    @Override
    public AbstractConstraints<X> or(AbstractConstraints<X> constraints) {
        if (constraints.isNone()) {
            return constraints;
        } else if (constraints.isAll()) {
            return this;
        } else if (constraints.isDisjunctive()) {
            final Disjunction<X> other = (Disjunction<X>) constraints;
            final ImmutableList.Builder<Monosemous<X>> builder = ImmutableList.builder();
            builder.addAll(this.options);
            builder.addAll(other.options);
            return new Disjunction<>(builder.build());
        } else {
            final Conjunction<X> other = (Conjunction<X>) constraints;
            final ImmutableList.Builder<Monosemous<X>> builder = ImmutableList.builder();
            builder.addAll(this.options);
            builder.add(other);
            return new Disjunction<>(builder.build());
        }
    }

    @Override
    public AbstractConstraints<X> reconcile(X context) {
        AbstractConstraints<X> reconciled = all();

        for (Monosemous<X> o : options) {
            reconciled = reconciled.or(o.reconcile(context));
        }

        return reconciled;
    }

    @Override
    public AbstractConstraints<X> simplify(X context) {
        AbstractConstraints<X> simplified = all();

        for (Monosemous<X> o : options) {
            simplified = simplified.or(o.simplify(context));
        }

        return simplified;
    }

    @Override
    public AbstractConstraints<X> establishSubtype(TypeMixin<X, ?> subtype, TypeMixin<X, ?> supertype) {
        return options.stream()
                .map(o -> o.establishSubtype(subtype, supertype))
                .reduce(AbstractConstraints.all(), AbstractConstraints::or);
    }

    @Override
    public boolean isSubtype(TypeMixin<X, ?> subtype, TypeMixin<X, ?> supertype) {
        return options.stream().allMatch(o -> o.isSubtype(subtype, supertype));
    }

    @Override
    public boolean areSubtypes(TypeList<X, ?> subtypes, TypeList<X, ?> supertypes) {
        return options.stream().anyMatch(o -> o.areSubtypes(subtypes, supertypes));
    }

    @Override
    public boolean areEqualTypes(Type<X, ?> a, Type<X, ?> b) {
        return options.stream().anyMatch(o -> o.areEqualTypes(a, b));
    }

    @Override
    public boolean areEqualTypes(AbstractTypeList<X, ?> as, AbstractTypeList<X, ?> bs) {
        return options.stream().anyMatch(o -> o.areEqualTypes(as, bs));
    }

    @Override
    public boolean contains(AbstractConstraints<X> cs) {
        // TODO this is just the cs:Conjunction case
        for (Monosemous<X> o : options) {
            if (o.contains(cs)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean checkBounds(X context, AbstractConstraints<X> contextualConstraints, Substitutions<X> substitutions) {
        throw null;
    }

    @Override
    public ImmutableList<ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>>> getConstraints() {
        final ImmutableList.Builder<ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>>> builder = ImmutableList.builder();

        for (Monosemous<X> o : options) {
            builder.addAll(o.getConstraints());
        }

        return builder.build();
    }

    public ImmutableList<Monosemous<X>> getOptions() {
        return options;
    }

    @Override
    public TypeParameterListImpl<X> getTypeParams() {
        return options.get(0).getTypeParams();
    }

    @Override
    public AbstractConstraints<X> substitute(X context, TypeParameterListImpl<X> parameters, Substitutions<X> substitutions) {
        return options.stream()
                .map(o -> o.substitute(context, parameters, substitutions))
                .reduce(none(), AbstractConstraints::or);
    }
}