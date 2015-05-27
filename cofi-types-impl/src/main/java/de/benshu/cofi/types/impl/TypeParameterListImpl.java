package de.benshu.cofi.types.impl;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import de.benshu.cofi.types.Constraints;
import de.benshu.cofi.types.Variance;
import de.benshu.cofi.types.bound.TypeParameter;
import de.benshu.cofi.types.bound.TypeParameterList;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.declarations.Interpreter;
import de.benshu.cofi.types.impl.declarations.TypeParameterListDeclaration;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Pair;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static de.benshu.cofi.types.impl.declarations.Interpreter.id;
import static de.benshu.cofi.types.impl.lists.AbstractTypeList.typeList;
import static de.benshu.commons.core.streams.Collectors.list;
import static java.util.stream.Collectors.joining;

public final class TypeParameterListImpl<X extends TypeSystemContext<X>> implements TypeParameterList<X> {
    private static final TypeParameterListImpl<?> EMPTY = new TypeParameterListImpl<>(null, AbstractConstraints.<SomeTypeSystemContext>none());

    @SuppressWarnings("unchecked")
    public static <X extends TypeSystemContext<X>> TypeParameterListImpl<X> empty() {
        return (TypeParameterListImpl<X>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <X extends TypeSystemContext<X>> TypeParameterListImpl<X> empty(X context, AbstractConstraints<X> parentConstraints) {
        return new TypeParameterListImpl<>(context, parentConstraints);
    }

    public static <X extends TypeSystemContext<X>> TypeParameterListImpl<X> createTrivial(int length, X context) {
        return createTrivial(describeN(length), context);
    }

    public static <X extends TypeSystemContext<X>> UnboundTypeParameterList<X> create(TypeParameterListDeclaration<X> declaration) {
        return new UnboundTypeParameterList<>(declaration);
    }

    public static ImmutableList<Pair<Variance, IndividualTags>> describeN(int length) {
        return IntStream.range(0, length)
                .mapToObj(i -> Pair.of(Variance.INVARIANT, IndividualTags.empty()))
                .collect(list());
    }

    public static <X extends TypeSystemContext<X>> TypeParameterListImpl<X> createTrivial(ImmutableList<Pair<Variance, IndividualTags>> params, X context) {
        final AtomicReference<TypeParameterListImpl<X>> hack = new AtomicReference<>();

        final UnboundTypeParameterList<X> original = create(new TypeParameterListDeclaration<X>() {
            @Override
            public <O> O supplyParameters(X context, Interpreter<ImmutableList<Pair<Variance, IndividualTags>>, O> interpreter) {
                return interpreter.interpret(params, context.getChecker());
            }

            @Override
            public <O> O supplyConstraints(X context, Interpreter<AbstractConstraints<X>, O> interpreter) {
                return interpreter.interpret(AbstractConstraints.trivial(context, hack.get()), context.getChecker());
            }
        });

        final TypeParameterListImpl<X> result = original.bind(context);
        hack.set(result);
        return result;
    }

    // TODO inline
    public static <X extends TypeSystemContext<X>> TypeParameterListImpl create(TypeParameterListDeclaration<X> declaration, X context) {
        return create(declaration).bind(context);
    }

    public static <X extends TypeSystemContext<X>> TypeParameterListImpl<X> rebind(de.benshu.cofi.types.TypeParameterList parameters) {
        return ((Unbound<?>) parameters).<X>rebind().bound;
    }

    private final UnboundTypeParameterList<X> original;
    private final X context;

    private final ImmutableList<TypeParameterImpl<X>> parameters;
    private final AbstractTypeList<X, TypeVariableImpl<X, ?>> variables;

    private volatile AbstractConstraints<X> constraints;

    public TypeParameterListImpl(UnboundTypeParameterList<X> original, X context) {
        final ImmutableList<Pair<Variance, IndividualTags>> parameterDeclarations = original.getDeclaration().supplyParameters(context, id());

        this.original = original;
        this.context = context;
        this.parameters = IntStream.range(0, parameterDeclarations.size())
                .mapToObj(i -> {
                    final Pair<Variance, IndividualTags> declaration = parameterDeclarations.get(i);
                    return new TypeParameterImpl<>(this, i, declaration.a, declaration.b, p -> ProperTypeVariableImpl.create(this, p));
                })
                .collect(list());
        this.variables = this.parameters.stream()
                .map(TypeParameterImpl::getVariable)
                .collect(typeList());
    }

    private TypeParameterListImpl(X context, AbstractConstraints<X> parentConstraints) {
        this.original = null;
        this.context = null;
        this.parameters = ImmutableList.of();
        this.variables = AbstractTypeList.empty();
        this.constraints = parentConstraints.isNone() ? parentConstraints
                : AbstractConstraints.trivial(context, parentConstraints, this);
    }

    X getContext() {
        return context;
    }

    boolean contains(TypeVariableImpl<X, ?> variable) {
        return variable.getParameterList().getUnbound() == original;
    }

    @Override
    public Iterator<TypeParameter<X>> iterator() {
        return Iterators.transform(parameters.iterator(), p -> p);
    }

    public Iterable<TypeParameterImpl<X>> iterable() {
        @SuppressWarnings("unchecked")
        final Iterable<TypeParameterImpl<X>> result = (Iterable<TypeParameterImpl<X>>) (Object) this;
        return result;
    }

    @Override
    public int size() {
        return parameters.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public TypeParameterImpl<X> get(int index) {
        return parameters.get(index);
    }

    @Override
    public AbstractConstraints<X> getConstraints() {
        if (constraints == null)
            synchronized (this) {
                if (constraints == null)
                    this.constraints = original.getDeclaration().supplyConstraints(context, id());
            }

        return constraints;
    }

    public AbstractTypeList<X, TypeVariableImpl<X, ?>> getVariables() {
        return variables;
    }

    @Override
    public String toString() {
        return debug();
    }

    private String debug() {
        final String parameters = getVariables().stream().map(v -> v.debug()).collect(joining(", "));
        final String constraints = Joiner.on(", ").join(new String[0]); // TODO debug constraints

        return parameters.isEmpty() && constraints.isEmpty()
                ? "\u3008\u3009"
                : constraints.isEmpty()
                ? "\u3008" + parameters + "\u3009"
                : "\u3008 " + parameters + " | " + constraints + " \u3009";
    }

    public Unbound<X> unbind() {
        return new Unbound<>(this);
    }

    public UnboundTypeParameterList<X> getUnbound() {
        return original;
    }

    public String toDescriptor() {
        return CharMatcher.WHITESPACE.removeFrom(debug());
    }

    public Stream<TypeParameterImpl<X>> stream() {
        return StreamSupport.stream(iterable().spliterator(), false);
    }

    private static class Unbound<X extends TypeSystemContext<X>> implements de.benshu.cofi.types.TypeParameterList {
        private final TypeParameterListImpl<X> bound;

        public Unbound(TypeParameterListImpl<X> bound) {
            this.bound = bound;
        }

        @Override
        public int size() {
            return bound.size();
        }

        @Override
        public boolean isEmpty() {
            return bound.isEmpty();
        }

        @Override
        public de.benshu.cofi.types.TypeParameter get(int index) {
            return bound.get(index).unbind();
        }

        @Override
        public Constraints getConstraints() {
            return bound.getConstraints().unbind();
        }

        @Override
        public Iterator<de.benshu.cofi.types.TypeParameter> iterator() {
            return FluentIterable.from(bound.iterable())
                    .transform(u -> (de.benshu.cofi.types.TypeParameter) u.unbind())
                    .iterator();
        }

        @Override
        public String debug() {
            return bound.debug();
        }

        public <Y extends TypeSystemContext<Y>> Unbound<Y> rebind() {
            return (Unbound<Y>) this;
        }
    }
}
