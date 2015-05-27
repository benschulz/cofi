package de.benshu.cofi.types.tags;

import de.benshu.commons.core.Optional;

import java.util.stream.Stream;

public interface IndividualTags {
    static IndividualTags empty() {
        return HashIndividualTags.empty();
    }

    static IndividualTags of(Entry<?> entry) {
        return HashIndividualTags.of(entry);
    }

    static <T> IndividualTags of(IndividualTag<T> tag, T value) {
        return HashIndividualTags.of(tag, value);
    }

    default boolean contains(IndividualTag<?> tag) {
        return !tryGet(tag).asSet().isEmpty();
    }

    default <T> T get(Tag<T> tag) {
        return tryGet(tag).get();
    }

    default <T> T getOrFallbackTo(Tag<T> tag, T fallback) {
        return tryGet(tag).getOrReturn(fallback);
    }

    default <T> T getOrFallbackToDefault(DefaultingTag<T> tag) {
        return tryGet(tag).getOrSupply(tag::getDefault);
    }

    default boolean isEmpty() {
        return size() == 0;
    }

    default <T> IndividualTags set(IndividualTag<T> tag, T value) {
        return setAll(HashIndividualTags.of(tag, value));
    }

    IndividualTags setAll(IndividualTags tags);

    int size();

    Stream<Entry<?>> stream();

    default <T> Optional<T> tryGet(Tag<T> tag) {
        return tryGet(tag);
    }

    interface Entry<T> extends Tags.Entry<T> {
        @Override
        IndividualTag<T> getTag();
    }
}
