package de.benshu.cofi.types.tags;

public interface Taggable<S extends Taggable<S>> extends Tagged {
    default <T> S setTag(IndividualTag<T> tag, T value) {
        return setTags(HashIndividualTags.of(tag, value));
    }

    S setTags(IndividualTags tags);
}
