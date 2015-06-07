package de.benshu.cofi.binary.internal;

public interface Constructor<T> {
    T construct(Ancestry ancestry);
}
