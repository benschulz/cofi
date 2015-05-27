package de.benshu.cofi.types;

import de.benshu.commons.core.Debuggable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface TypeList<T extends Type> extends Iterable<T>, Debuggable {
    static <T extends Type> TypeList<T> empty() {
        return new ArrayTypeList<>();
    }

    static <T extends Type> TypeList<T> of(T... types) {
        return new ArrayTypeList<>(types.clone());
    }

    T get(int index);

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    default Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED), false);
    }

    TypeList<T> subList(int fromIndex, int toIndex);

    default TypeList<T> subList(int fromIndex) {
        return subList(fromIndex, size());
    }

    class ArrayTypeList<T extends Type> implements TypeList<T> {
        private final T[] types;

        @SafeVarargs
        public ArrayTypeList(T... types) {
            this.types = types;
        }

        @Override
        public T get(int index) {
            return types[index];
        }

        @Override
        public int size() {
            return types.length;
        }

        @Override
        public TypeList<T> subList(int fromIndex, int toIndex) {
            return of(Arrays.copyOfRange(types, fromIndex, toIndex));
        }

        @Override
        public Iterator<T> iterator() {
            return Arrays.asList(types).iterator();
        }

        @Override
        public String debug() {
            return "\u3008" + stream().map(Debuggable::debug).collect(Collectors.joining(", ")) + "\u3009";
        }
    }
}
