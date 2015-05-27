package de.benshu.cofi.types.impl.constraints;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import de.benshu.cofi.types.Variance;
import de.benshu.cofi.types.impl.intersections.AbstractIntersectionTypeConstructor;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.unions.AbstractUnionTypeConstructor;
import de.benshu.cofi.types.impl.intersections.AnonymousIntersectionType;
import de.benshu.cofi.types.impl.unions.AnonymousUnionType;
import de.benshu.cofi.types.impl.Bottom;
import de.benshu.cofi.types.impl.ConstructedTypeMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.TypeVariableImpl;
import de.benshu.cofi.types.impl.UnboundTypeParameterList;
import de.benshu.cofi.types.impl.declarations.Interpreter;
import de.benshu.cofi.types.impl.declarations.TypeParameterListDeclaration;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.commons.core.streams.Collectors.map;
import static de.benshu.commons.core.streams.Collectors.setMultimap;

public final class Conjunction<X extends TypeSystemContext<X>> extends Monosemous<X> {
    private static <X extends TypeSystemContext<X>> ImmutableMap<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> getEquivalents(
            Iterable<Map.Entry<TypeVariableImpl<X, ?>, Constraint<X>>> constraints,
            ImmutableMap<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> equivalentsA,
            ImmutableMap<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> equivalentsB) {
        final SetMultimap<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> subtypes = HashMultimap.create();
        for (Map.Entry<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> entry : Iterables.concat(equivalentsA.entrySet(),
                equivalentsB.entrySet())) {
            subtypes.put(entry.getKey(), entry.getValue());
            subtypes.put(entry.getValue(), entry.getKey());
        }

        for (Entry<TypeVariableImpl<X, ?>, Constraint<X>> entry : constraints) {
            final Constraint<X> c = entry.getValue();

            if (c.getKind() != Constraint.Kind.META && c.getBound() instanceof TypeVariableImpl<?, ?>) {
                final TypeVariableImpl<X, ?> bound = (TypeVariableImpl<X, ?>) c.getBound();

                if (c.getKind() == Constraint.Kind.LOWER) {
                    subtypes.put(entry.getKey(), bound);
                } else {
                    subtypes.put(bound, entry.getKey());
                }
            }
        }
        closingWrtTransitivity:
        while (true) {
            for (Map.Entry<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> entry : subtypes.entries()) {
                if (subtypes.putAll(entry.getKey(), subtypes.get(entry.getValue()))) {
                    continue closingWrtTransitivity;
                }
            }

            break;
        }

        final Map<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> equivalents = new HashMap<>();

        equivalents.putAll(equivalentsA);

        for (Map.Entry<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> entry : equivalentsB.entrySet()) {
            if (!entry.getKey().equals(equivalents.get(entry.getValue()))) {
                equivalents.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> entry : subtypes.entries()) {
            final TypeVariableImpl<X, ?> key = entry.getKey();
            final TypeVariableImpl<X, ?> val = entry.getValue();

            if (!key.equals(val) && subtypes.containsEntry(val, key) && val.equals(equivalents.get(val))) {
                equivalents.put(key, val);
            }
        }
        normalizingEquivalents:
        while (true) {
            for (Map.Entry<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> entry : equivalents.entrySet()) {
                final TypeVariableImpl<X, ?> transitive = equivalents.get(entry.getValue());

                if (!entry.getValue().equals(transitive)) {
                    equivalents.put(entry.getKey(), transitive);
                    continue normalizingEquivalents;
                }
            }

            break;
        }

        return ImmutableMap.copyOf(equivalents);
    }

    private static <X extends TypeSystemContext<X>> ImmutableMap<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> identityEquivalents(
            ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>> constraints) {
        ImmutableMap.Builder<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> builder = ImmutableMap.builder();

        for (TypeVariableImpl<X, ?> var : constraints.keySet()) {
            builder.put(var, var);
        }

        return builder.build();
    }

    private static <X extends TypeSystemContext<X>> AbstractConstraints<X> newConjunction(X context, Monosemous<X> parent, TypeParameterListImpl<X> params, TypeVariableImpl<X, ?> var,
                                                                                          Constraint<X> constraint) {
        final TypeSystemImpl<X> types = context.getTypeSystem();
        final Constraint<X> upper = Constraint.upper(types.getTop());
        final Constraint<X> lower = Constraint.lower(types.getBottom());

        switch (constraint.getKind()) {
            case META:
                final ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>> constraints = ImmutableSetMultimap.of(
                        var, constraint,
                        var, upper,
                        var, lower
                );

                return new Conjunction<>(parent, params, constraints);
            case LOWER:
                return newNormalizedConjunction(context, parent, params, var, constraint, upper, Constraint.upper(var), lower);
            case UPPER:
                return newNormalizedConjunction(context, parent, params, var, constraint, lower, Constraint.lower(var), upper);
            default:
                throw new UnsupportedOperationException("Constraint kind: " + constraint.getKind());
        }
    }

    private static <X extends TypeSystemContext<X>> AbstractConstraints<X> newNormalizedConjunction(X context, Monosemous<X> parent, TypeParameterListImpl<X> params,
                                                                                                    TypeVariableImpl<X, ?> var, Constraint<X> constraint, Constraint<X> normalization, Constraint<X> reverse,
                                                                                                    Constraint<X> rNormalization) {
        if (var.equals(constraint.getBound())) {
            return new Conjunction<>(parent, params, ImmutableSetMultimap.<TypeVariableImpl<X, ?>, Constraint<X>>of(var,
                    Constraint.upper(context.getTypeSystem().getTop())));
        }

        final ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>> constraints;
        if (constraint.getBound() instanceof TypeVariableImpl<?, ?>) {
            final TypeVariableImpl<X, ?> rVar = (TypeVariableImpl<X, ?>) constraint.getBound();
            constraints = ImmutableSetMultimap.of(
                    var, constraint,
                    var, normalization,
                    rVar, reverse,
                    rVar, rNormalization);

        } else {
            constraints = ImmutableSetMultimap.of(
                    var, constraint,
                    var, normalization
            );
        }

        return new Conjunction<>(parent, params, constraints);
    }

    private final Monosemous<X> parent;
    private final TypeParameterListImpl<X> params;
    private final ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>> constraints;
    /**
     * Given two type vars A and B and the constraints A <: B and B <: A, the variables A and B are
     * effectively equal (A = B). This map will reflect this equivalence with <strong>one</strong>
     * mapping, <em>either</em> A &#x21A6; B or B &#x21A6; A.
     * <p/>
     * For any type var X in {@code params} this map contains X &#x21A6; X.
     */
    private final ImmutableMap<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> equivalents;
    private final boolean reconciled;
    private volatile AbstractConstraints<X> simplified = null;

    Conjunction(Monosemous<X> parent, TypeParameterListImpl<X> params,
                ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>> constraints) {
        this(parent, params, constraints, identityEquivalents(constraints));
    }

    Conjunction(Monosemous<X> parent, TypeParameterListImpl<X> params,
                ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>> constraints,
                ImmutableMap<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> equivalents) {
        this(parent, params, constraints, equivalents, false, false);
    }

    private Conjunction(Monosemous<X> parent, TypeParameterListImpl<X> params,
                        ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>> constraints,
                        ImmutableMap<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> equivalents, boolean reconciled, boolean simplified) {
        checkArgument(params.size() >= constraints.keySet().size());
        checkArgument(params.size() >= equivalents.size());

        checkArgument(!params.isEmpty() || !parent.getTypeParams().isEmpty());

        this.parent = parent;
        this.params = params;
        this.constraints = constraints;
        this.equivalents = equivalents;
        this.reconciled = reconciled;
        this.simplified = simplified ? this : null;
    }

    @Override
    public Monosemous<X> getParent() {
        return parent;
    }

    Conjunction<X> and(Conjunction<X> other) {
        if (this.equals(other)) {
            return this;
        }

        final Iterable<Map.Entry<TypeVariableImpl<X, ?>, Constraint<X>>> both = Iterables.concat(this.constraints.entries(),
                other.constraints.entries());

        final ImmutableMap<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> equivalents = getEquivalents(both, this.equivalents,
                other.equivalents);

        final ImmutableSetMultimap.Builder<TypeVariableImpl<X, ?>, Constraint<X>> builder = ImmutableSetMultimap.builder();

        for (Map.Entry<TypeVariableImpl<X, ?>, Constraint<X>> entry : both) {
            final TypeVariableImpl<X, ?> newKey = equivalents.get(entry.getKey());
            final Constraint<X> val = entry.getValue();

            if (val.getKind() == Constraint.Kind.META || !newKey.equals(equivalents.get(val.getBound()))) {
                builder.put(newKey, entry.getValue());
            }
        }

        return new Conjunction<>(parent, params, builder.build(), equivalents);
    }

    @Override
    public AbstractConstraints<X> and(AbstractConstraints<X> constraints) {
        if (constraints.isNone()) {
            return this;
        } else if (constraints.isAll()) {
            return constraints;
        } else if (constraints.isDisjunctive()) {
            return constraints.and(this);
        } else {
            return and((Conjunction<X>) constraints);
        }
    }

    public ImmutableSet<Constraint<X>> getConstraints(TypeVariableImpl<X, ?> var) {
        final TypeVariableImpl<X, ?> equivalent = equivalents.get(var);
        return equivalent == null
                ? parent.getConstraints(var)
                : this.constraints.get(equivalent);
    }

    @Override
    public boolean isAll() {
        return false;
    }

    @Override
    public boolean isDisjunctive() {
        return false;
    }

    @Override
    public boolean isNone() {
        return false;
    }

    @Override
    public AbstractConstraints<X> or(AbstractConstraints<X> constraints) {
        if (constraints.isNone()) {
            return none();
        } else if (constraints.isAll()) {
            return this;
        } else if (constraints.isDisjunctive()) {
            return constraints.or(this);
        } else {
            return new Disjunction<>(ImmutableList.of(this, (Conjunction<X>) constraints));
        }
    }

    @Override
    public AbstractConstraints<X> reconcile(X context) {
        AbstractConstraints<X> result = new Conjunction<>(parent, params, constraints, equivalents, true, false);

        for (TypeVariableImpl<X, ?> var : constraints.keySet()) {
            ImmutableSet<Constraint<X>> varConstraints = constraints.get(var);

            for (Constraint<X> constraint : varConstraints) {
                for (Constraint<X> other : varConstraints) {
                    final AbstractConstraints<X> newResult = constraint.reconcile(context, other, this);

                    if (!newResult.contains(this)) {
                        // constraint.reconcile(other, this); // here for debugging
                        result = result.and(newResult);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public AbstractConstraints<X> simplify(X context) {
        if (simplified != null) {
            return simplified;
        }

        if (!reconciled) {
            return simplified = reconcile(context).simplify(context);
        }

        ImmutableSetMultimap.Builder<TypeVariableImpl<X, ?>, Constraint<X>> builder = ImmutableSetMultimap.builder();

        for (TypeVariableImpl<X, ?> var : constraints.keySet()) {
            for (Constraint<X> constraint : constraints.get(var)) {
                boolean implied = false;
                boolean reflexively = true;

                for (Constraint<X> other : constraints.get(var)) {
                    final boolean impliedBy = other != constraint && other.implies(context, constraint, this);

                    implied |= impliedBy;
                    reflexively &= !impliedBy || constraint.implies(context, other, this);
                }

                if (!implied || reflexively) {
                    builder.put(var, constraint);
                }
            }
        }

        return simplified = new Conjunction<>(parent, params, builder.build(), equivalents, true, true);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("(");
        final String AND = " and ";

        for (Map.Entry<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> entry : equivalents.entrySet()) {
            if (!entry.getKey().equals(entry.getValue())) {
                builder.append(entry.getKey());
                builder.append(" := ");
                builder.append(entry.getValue());
                builder.append(AND);
            }
        }

        for (TypeVariableImpl<X, ?> var : constraints.keySet()) {
            for (Constraint<X> constraint : constraints.get(var)) {
                builder.append(constraint.toString(var));
                builder.append(AND);
            }
        }

        builder.append(parent);

        builder.append(')');
        return builder.toString();
    }

    @Override
    public boolean includesLowerBound(X context, TypeVariableImpl<X, ?> variable, TypeMixin<X, ?> bound) {
        return getConstraints(variable).stream()
                .anyMatch(c -> c.getKind() == Constraint.Kind.LOWER && isSame(context, c.getBound(), bound));
    }

    @Override
    public boolean includesUpperBound(X context, TypeVariableImpl<X, ?> variable, TypeMixin<X, ?> bound) {
        return getConstraints(variable).stream()
                .anyMatch(c -> c.getKind() == Constraint.Kind.UPPER && isSame(context, c.getBound(), bound));
    }

    private boolean isSame(X context, TypeMixin<X, ?> a, TypeMixin<X, ?> b) {
        if (a == b)
            return true;
        else if(a instanceof TypeVariableImpl && b instanceof TypeVariableImpl)
            return a.equals(b);
        else if (a instanceof ConstructedTypeMixin && b instanceof ConstructedTypeMixin)
            return isSame(context, (ConstructedTypeMixin<X, ?, ?>) a, (ConstructedTypeMixin<X, ?, ?>) b);
        else if (a instanceof TypeConstructorMixin && b instanceof TypeConstructorMixin)
            return isSame((TypeConstructorMixin<X, ?, ?>) a, (TypeConstructorMixin<X, ?, ?>) b);
        else
            return a instanceof Bottom && b instanceof  Bottom || a instanceof Error && b instanceof  Error;
    }

    private boolean isSame(X context, ConstructedTypeMixin<X, ?, ?> a, ConstructedTypeMixin<X, ?, ?> b) {
        if (!isSame(a.getConstructor(), b.getConstructor()))
            return false;

        AbstractTypeList<X, ?> aArgs = a.getArguments();
        AbstractTypeList<X, ?> bArgs = b.getArguments();

        for (int i = 0; i < aArgs.size(); ++i)
            if (!isSame(context, aArgs.get(i), bArgs.get(i)))
                return false;
        return true;
    }

    private boolean isSame(TypeConstructorMixin<X, ?, ?> a, TypeConstructorMixin<X, ?, ?> b) {
        if (a == b)
            return true;
        else if (a instanceof AbstractIntersectionTypeConstructor && b instanceof AbstractIntersectionTypeConstructor)
            return ((AbstractIntersectionTypeConstructor<?>) a).getOriginal() == ((AbstractIntersectionTypeConstructor<?>) b).getOriginal();
        else if (a instanceof AbstractTemplateTypeConstructor && b instanceof AbstractTemplateTypeConstructor)
            return ((AbstractTemplateTypeConstructor<?>) a).getOriginal() == ((AbstractTemplateTypeConstructor<?>) b).getOriginal();
        else if (a instanceof AbstractUnionTypeConstructor && b instanceof AbstractUnionTypeConstructor)
            return ((AbstractUnionTypeConstructor<?>) a).getOriginal() == ((AbstractUnionTypeConstructor<?>) b).getOriginal();
        else
            return false;
    }

    @Override
    public AbstractConstraints<X> establishSubtype(TypeMixin<X, ?> subtype, TypeMixin<X, ?> supertype) {
        return subtype.establishSubtype(supertype, this);
    }

    @Override
    public boolean isSubtype(TypeMixin<X, ?> subtype, TypeMixin<X, ?> supertype) {
        return establishSubtype(subtype, supertype) == this;
    }

    @Override
    public boolean contains(AbstractConstraints<X> cs) {
        return equals(cs);
    }

    @Override
    public Conjunction<X> and(X context, TypeVariableImpl<X, ?> var, Constraint<X> c) {
        return (Conjunction<X>) and(newConjunction(context, parent, params, var, c));
    }

    @Override
    public boolean checkBounds(X context, AbstractConstraints<X> contextualConstraints, Substitutions<X> substitutions) {
        for (int i = 0; i < params.size(); ++i) {
            TypeVariableImpl<X, ?> variable = params.get(i).getVariable();
            for (Constraint<X> c : getConstraints(variable)) {
                if (!c.check(context, contextualConstraints, substitutions, variable)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public ImmutableList<ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>>> getConstraints() {
        // TODO equivalents and parent(s) need to be considered
        return ImmutableList.of(constraints);
    }

    ImmutableMap<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> getEquivalents() {
        return equivalents;
    }

    ImmutableSet<ImmutableSet<TypeVariableImpl<X, ?>>> getQuotientSet() {
        ImmutableSetMultimap.Builder<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> builder = ImmutableSetMultimap.builder();

        for (Map.Entry<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> entry : getEquivalents().entrySet()) {
            builder.put(entry.getValue(), entry.getKey());
        }

        @SuppressWarnings("unchecked")
        // guaranteed by ImmutableSetMultimap
                ImmutableCollection<ImmutableSet<TypeVariableImpl<X, ?>>> equivalenceClasses = (ImmutableCollection<ImmutableSet<TypeVariableImpl<X, ?>>>) (Object) builder
                .build().asMap().values();
        return ImmutableSet.copyOf(equivalenceClasses);
    }

    ImmutableSetMultimap<TypeVariableImpl<X, ?>, Constraint<X>> getMonoConstraints() {
        return constraints;
    }

    @Override
    public TypeMixin<X, ?> getUpperBound(X context, TypeVariableImpl<X, ?> variable) {
        if (!variable.getParameter().getList().equals(params)) {
            return parent.getUpperBound(context, variable);
        }

        return AnonymousIntersectionType.createIfNonTrivial(context, getUppers(variable).map(t -> (ProperTypeMixin<X, ?>) t));
    }

    @Override
    public TypeMixin<X, ?> getLowerBound(X context, TypeVariableImpl<X, ?> variable) {
        if (!variable.getParameter().getList().equals(params)) {
            return parent.getLowerBound(context, variable);
        }

        return AnonymousUnionType.create(context, getUppers(variable).map(t -> (ProperTypeMixin<X, ?>) t));
    }

    @Override
    public TypeParameterListImpl<X> getTypeParams() {
        return params;
    }

    @Override
    public AbstractConstraints<X> substitute(X context, TypeParameterListImpl<X> parameters, Substitutions<X> substitutions) {
        if(getTypeParams().isEmpty())
            return parent.getTypeParams().isEmpty() ? parent : TypeParameterListImpl.empty(context, parameters.getConstraints()).getConstraints();

        final AtomicReference<AbstractConstraints<X>> hack = new AtomicReference<>();

        final UnboundTypeParameterList<X> unbound = TypeParameterListImpl.create(new TypeParameterListDeclaration<X>() {
            @Override
            public <O> O supplyParameters(X context, Interpreter<ImmutableList<Pair<Variance, IndividualTags>>, O> interpreter) {
                return getTypeParams().getUnbound().getDeclaration().supplyParameters(context, interpreter);
            }

            @Override
            public <O> O supplyConstraints(X context, Interpreter<AbstractConstraints<X>, O> interpreter) {
                return interpreter.interpret(hack.get(), context.getChecker());
            }
        });

        final TypeParameterListImpl<X> newTypeParameters = unbound.bind(context);

        Function<TypeVariableImpl<X,?>, TypeVariableImpl<X, ?>> mapVariable = v -> newTypeParameters.getVariables().get(v.getParameter().getIndex());

        hack.set(parameters.getConstraints().append(new Conjunction<>(
                                none(),
                                newTypeParameters,
                                constraints.entries().stream()
                                        .map(e -> immutableEntry(
                                                mapVariable.apply(e.getKey()),
                                                e.getValue().substitute(context, substitutions)))
                                        .collect(setMultimap()),
                                equivalents.entrySet().stream()
                                        .map(e -> immutableEntry(
                                                mapVariable.apply(e.getKey()),
                                                mapVariable.apply(e.getValue())
                                        ))
                                        .collect(map())
                        )
                )
        );

        return hack.get();
    }

    @Override
    // TODO inline (slightly leaky abstraction)
    public AbstractConstraints<X> transferTo(X context, AbstractConstraints<X> target, int fromIndex) {
        AbstractTypeList<X, TypeVariableImpl<X, ?>> targetVars = target.getTypeParams().getVariables()
                .subList(fromIndex, fromIndex + getTypeParams().size());

        return transferTo(context, target, targetVars);
    }

    public AbstractConstraints<X> transferTo(X context, AbstractConstraints<X> target, AbstractTypeList<X, ?> arguments) {
        checkArgument(getTypeParams().size() == arguments.size());

        ImmutableBiMap.Builder<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> builder = ImmutableBiMap.builder();

        for (int i = 0; i < arguments.size(); ++i) {
            TypeMixin<X, ?> arg = arguments.get(i);

            if (arg instanceof TypeVariableImpl<?, ?>) {
                builder.put(getTypeParams().getVariables().get(i), (TypeVariableImpl<X, ?>) arg);
            }
        }

        return transferTo(context, target, arguments, builder.build());
    }

    private AbstractConstraints<X> transferTo(X context, AbstractConstraints<X> target, AbstractTypeList<X, ?> arguments,
                                              ImmutableBiMap<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> variableMapping) {

        ImmutableMap.Builder<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> equivalentsBuilder = ImmutableMap.builder();
        ImmutableSetMultimap.Builder<TypeVariableImpl<X, ?>, Constraint<X>> constraintsBuilder = ImmutableSetMultimap.builder();

        for (Map.Entry<TypeVariableImpl<X, ?>, TypeVariableImpl<X, ?>> varMapping : variableMapping.entrySet()) {
            TypeVariableImpl<X, ?> var = varMapping.getKey();
            TypeVariableImpl<X, ?> targetVar = varMapping.getValue();

            equivalentsBuilder.put(targetVar, variableMapping.get(getEquivalents().get(var)));

            Substitutions<X> substitutions = Substitutions.ofThrough(getTypeParams(), arguments);
            for (Constraint<X> c : getConstraints(var)) {
                constraintsBuilder.put(targetVar, c.substitute(context, substitutions));
            }
        }

        return target.and(new Conjunction<>(null, target.getTypeParams(), constraintsBuilder.build(), equivalentsBuilder.build()));
    }

}