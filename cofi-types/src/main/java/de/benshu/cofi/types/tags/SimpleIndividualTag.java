package de.benshu.cofi.types.tags;

import de.benshu.commons.core.Optional;

import java.util.function.BinaryOperator;
import java.util.function.Function;

public class SimpleIndividualTag<T> implements IndividualTag<T> {
    static <T> SimpleIndividualTag<T> named(String name) {
        return new SimpleIndividualTag<>(name);
    }

    private final String name;

    private SimpleIndividualTag(String name) {
        this.name = name;
    }

    @Override
    public String debug() {
        return name;
    }

    public <U> SimpleDefaultingIndividualTag<U> defaultingTo(U defaultValue) {
        return SimpleDefaultingIndividualTag.create(name, defaultValue);
    }

    public <U> SemigroupTag.Individual<U> semigroup(BinaryOperator<U> combiner) {
        return semigroup(combiner, null);
    }

    public <U> SemigroupTag.Individual<U> semigroup(BinaryOperator<U> combiner, Function<Tags, Optional<U>> directDerivation) {
        return new SemigroupTag.Individual<>(name, combiner, directDerivation);
    }

    public <U> IndividualTag<U> unambiguouslyDerivable() {
        return new UnambiguouslyDerivableTag.Individual<>(name, null);
    }

    public <U> IndividualTag<U> unambiguouslyDerivable(Function<Tags, Optional<U>> directDerivation) {
        return new UnambiguouslyDerivableTag.Individual<>(name, directDerivation);
    }
}
