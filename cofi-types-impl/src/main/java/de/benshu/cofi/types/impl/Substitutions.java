package de.benshu.cofi.types.impl;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;

public class Substitutions<X extends TypeSystemContext<X>> implements Iterable<Substitution<X>> {
    public static <X extends TypeSystemContext<X>> Substitutions<X> empty() {
        AbstractTypeList<X, ?> empty = AbstractTypeList.empty();
        return ofThrough(TypeParameterListImpl.empty(), empty);
    }

    public static <X extends TypeSystemContext<X>> Substitutions<X> ofThrough(TypeParameterListImpl<X> parameters, AbstractTypeList<X, ?> substitutes) {
        return new Substitutions<>(parameters.getVariables(), substitutes);
    }

    public static <X extends TypeSystemContext<X>> Substitutions<X> trivialOf(TypeParameterListImpl<X> parameters) {
        return ofThrough(parameters, parameters.getVariables());
    }

    public static <X extends TypeSystemContext<X>> Substitutions<X> firstOfThrough(TypeParameterListImpl<X> parameters, AbstractTypeList<X, ?> substitutes) {
        return new Substitutions<>(parameters.getVariables().subList(0, substitutes.size()), substitutes);
    }

    public static <X extends TypeSystemContext<X>> Substitutions<X> from(ImmutableMap<? extends TypeVariableImpl<X, ?>, ? extends TypeMixin<X, ?>> substitutions) {
        return new Substitutions<>(
                AbstractTypeList.of(substitutions.keySet()),
                AbstractTypeList.of(substitutions.values())
        );
    }

    private final AbstractTypeList<X, TypeVariableImpl<X, ?>> variables;
    private final AbstractTypeList<X, ?> substitutes;

    public Substitutions(AbstractTypeList<X, TypeVariableImpl<X, ?>> variables, AbstractTypeList<X, ?> substitutes) {
        checkArgument(variables.size() == substitutes.size());

        this.variables = variables;
        this.substitutes = substitutes;
    }

    @Override
    public Iterator<Substitution<X>> iterator() {
        return new AbstractIterator<Substitution<X>>() {
            private int i = 0;

            @Override
            protected Substitution<X> computeNext() {
                if (i >= variables.size()) return endOfData();
                return Substitution.ofThrough(variables.get(i), substitutes.get(i++));
            }
        };
    }

    public Stream<Substitution<X>> stream() {
        return StreamSupport.stream(Spliterators.spliterator(iterator(), variables.size(), 0), false);
    }

    public TypeMixin<X, ?> substitute(TypeVariableImpl<X, ?> variable) {
        return stream().filter(s -> s.getVariable().equals(variable)).map(Substitution::getSubstitute).findAny().orElse(variable);
    }

    public boolean substitutes(TypeVariableImpl<X, ?> variable){
        return stream().filter(s -> s.getVariable().equals(variable)).map(Substitution::getSubstitute).findAny().isPresent();
    }

    public int size() {
        return variables.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

}
