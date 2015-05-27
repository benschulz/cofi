package de.benshu.cofi.types.tags;

public interface DefaultingTag<T> extends Tag<T> {
    T getDefault();
}
