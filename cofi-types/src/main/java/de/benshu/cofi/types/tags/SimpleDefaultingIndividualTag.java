package de.benshu.cofi.types.tags;

import de.benshu.commons.core.Optional;

import java.util.Objects;
import java.util.function.Function;

public final class SimpleDefaultingIndividualTag<T> implements DefaultingIndividualTag<T> {
    static <T> SimpleDefaultingIndividualTag<T> create(String name, T defaultValue) {
        return new SimpleDefaultingIndividualTag<>(name, defaultValue);
    }

    private final String name;
    private final T defaultValue;

    private SimpleDefaultingIndividualTag(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public T getDefault() {
        return defaultValue;
    }

    @Override
    public String debug() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public MonoidTag.Individual<T> inheritedEqually() {
        return inheritedEqually(null);
    }

    public MonoidTag.Individual<T> inheritedEqually(Function<Tags, Optional<T>> directDerivation) {
        return new MonoidTag.Individual<>(name, SimpleDefaultingIndividualTag::eql, defaultValue, directDerivation);
    }

    private static <T> T eql(T a, T b) {
        if (a != b && !Objects.equals(a, b))
            throw new AssertionError(); // TODO error handling strategy
        return a;
    }
}
