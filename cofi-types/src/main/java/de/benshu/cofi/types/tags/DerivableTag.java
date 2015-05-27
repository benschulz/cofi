package de.benshu.cofi.types.tags;

import de.benshu.commons.core.Optional;

import java.util.function.BinaryOperator;
import java.util.function.Function;

public interface DerivableTag<T> extends Tag<T> {
    static Builder named(String name) {
        return new Builder(name);
    }

    class Builder {
        private final String name;

        public Builder(String name) {
            this.name = name;
        }

        public <T> SemigroupTag<T> semigroup(BinaryOperator<T> combiner) {
            return semigroup(combiner, null);
        }

        public <T> SemigroupTag<T> semigroup(BinaryOperator<T> combiner, Function<Tags, Optional<T>> directDerivation) {
            return new SemigroupTag<>(name, combiner, directDerivation);
        }
    }
}
