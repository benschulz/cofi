package de.benshu.cofi.types.tags;

import de.benshu.commons.core.Optional;

import java.util.function.BinaryOperator;
import java.util.function.Function;

public class MonoidTag<T> extends SemigroupTag<T> implements DefaultingTag<T> {
    private final T identity;

    private MonoidTag(String name, BinaryOperator<T> operator, T identity, Function<Tags, Optional<T>> directDerivation) {
        super(name, operator, directDerivation);

        this.identity = identity;
    }

    @Override
    public T getDefault() {
        return identity;
    }

    public static class Individual<T> extends MonoidTag<T> implements DefaultingIndividualTag<T> {
        Individual(String name, BinaryOperator<T> operator, T identity, Function<Tags, Optional<T>> directDerivation) {
            super(name, operator, identity, directDerivation);
        }
    }
}
