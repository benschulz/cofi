package de.benshu.cofi.types.bound;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface TypeList<X, T extends Type<X, ?>> extends Iterable<T> {
    T get(int index);

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    default Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED), false);
    }
}
