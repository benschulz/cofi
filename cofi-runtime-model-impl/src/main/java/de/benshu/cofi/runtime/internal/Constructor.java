package de.benshu.cofi.runtime.internal;

public interface Constructor<T> {
    T construct(Ancestry ancestry);
}
