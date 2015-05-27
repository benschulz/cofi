package de.benshu.cofi.types.tags;

public interface IndividualTag<T> extends Tag<T> {
    static <T> SimpleIndividualTag<T> named(String name) {
        return SimpleIndividualTag.named(name);
    }
}
