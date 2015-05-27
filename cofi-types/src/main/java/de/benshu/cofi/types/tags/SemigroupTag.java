package de.benshu.cofi.types.tags;

import com.google.common.collect.ImmutableSet;
import de.benshu.commons.core.Optional;

import java.util.function.BinaryOperator;
import java.util.function.Function;

import static de.benshu.commons.core.Optional.some;

public class SemigroupTag<T>
        implements Tag<T>,
                   ApplicationDerivableTag<T>,
                   DirectlyDerivableTag<T>,
                   InheritanceDerivableTag<T>,
                   IntersectionDerivableTag<T>,
                   RefinementDerivableTag<T>,
                   SubstitutionDerivableTag<T> {

    private final String name;
    private final BinaryOperator<T> operator;
    private final Function<Tags, Optional<T>> directDerivation;

    SemigroupTag(String name, BinaryOperator<T> operator, Function<Tags, Optional<T>> directDerivation) {
        this.name = name;
        this.operator = operator;
        this.directDerivation = directDerivation;
    }

    @Override
    public String debug() {
        return name;
    }

    @Override
    public Optional<T> tryDeriveDirectly(Tags tags) {
        return Optional.from(directDerivation)
                .flatMap(d -> d.apply(tags));
    }

    @Override
    public Optional<T> tryDeriveFromApplication(Tags unapplied, Tags applied) {
        return unapplied.tryGet(this);
    }

    @Override
    public Optional<T> tryDeriveFromInheritance(Tags inherited, Tags all) {
        return inherited.tryGet(this);
    }

    @Override
    public Optional<T> tryDeriveFromIntersection(ImmutableSet<Tags> elements, Tags intersected) {
        final Optional<T> any = elements.iterator().next().tryGet(this);

        return elements.stream()
                .map(e -> e.tryGet(this))
                .reduce(any, this::applyOperator);
    }

    @Override
    public Optional<T> tryDeriveFromRefinement(Tags unrefined, IndividualTags refinement, Tags refined) {
        for (T r : refinement.tryGet(this))
            return applyOperator(some(r), unrefined.tryGet(this));
        return unrefined.tryGet(this);
    }

    @Override
    public Optional<T> tryDeriveFromSubstitution(Tags unsubstituted, Tags substituted) {
        return unsubstituted.tryGet(this);
    }

    private Optional<T> applyOperator(Optional<T> a, Optional<T> b) {
        for (T valueA : a) {
            for (T valueB : b)
                return some(operator.apply(valueA, valueB));

            return a;
        }
        return b;
    }

    public static class Individual<T> extends SemigroupTag<T> implements IndividualTag<T> {
        Individual(String name, BinaryOperator<T> operator, Function<Tags, Optional<T>> directDerivation) {
            super(name, operator, directDerivation);
        }
    }
}
