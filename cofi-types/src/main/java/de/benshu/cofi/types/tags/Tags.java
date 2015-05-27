package de.benshu.cofi.types.tags;

import de.benshu.commons.core.Optional;

import java.util.stream.Stream;

public interface Tags {
    default <T> T get(Tag<T> tag) {
        return tryGet(tag).get();
    }

    default IndividualTags getIndividualTags() {
        return stream()
                .filter(e -> e.getTag() instanceof IndividualTag<?>)
                .map(e -> new HashIndividualTags.Entry<Object>() {
                    @Override
                    public IndividualTag<Object> getTag() {
                        return (IndividualTag<Object>) e.getTag();
                    }

                    @Override
                    public Object getValue() {
                        return e.getValue();
                    }
                })
                .collect(HashIndividualTags.collector());
    }

    default <T> T getOrFallbackTo(Tag<T> tag, T fallback) {
        return tryGet(tag).getOrReturn(fallback);
    }

    default <T> T getOrFallbackToDefault(DefaultingTag<T> tag) {
        return tryGet(tag).getOrSupply(tag::getDefault);
    }

    <T> Optional<T> getTagged(Class<? extends T> expectedType);

    default <T> Tags set(IndividualTag<T> tag, T value) {
        return setAll(HashIndividualTags.of(tag, value));
    }

    Tags setAll(IndividualTags tags);

    Stream<? extends Entry<?>> stream();

    <T> Optional<T> tryGet(Tag<T> tag);

    interface Entry<T> {
        Tag<T> getTag();

        T getValue();
    }
}
