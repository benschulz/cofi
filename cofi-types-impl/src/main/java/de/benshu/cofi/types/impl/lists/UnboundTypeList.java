package de.benshu.cofi.types.impl.lists;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.Type;
import de.benshu.cofi.types.TypeList;
import de.benshu.commons.core.Debuggable;

import java.util.Iterator;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static de.benshu.commons.core.streams.Collectors.list;
import static java.util.stream.Collectors.collectingAndThen;

public class UnboundTypeList<T extends Type> implements TypeList<T> {
    public static <T extends Type> Collector<T, ?, UnboundTypeList<T>> unboundTypeList() {
        return collectingAndThen(list(), UnboundTypeList::new);
    }

    private final ImmutableList<T> types;

    UnboundTypeList(ImmutableList<T> types) {
        this.types = types;
    }

    @Override
    public T get(int index) {
        return types.get(index);
    }

    @Override
    public int size() {
        return types.size();
    }

    @Override
    public TypeList<T> subList(int fromIndex, int toIndex) {
        return new UnboundTypeList<>(types.subList(fromIndex, toIndex));
    }

    @Override
    public Iterator<T> iterator() {
        return types.iterator();
    }

    @Override
    public String toString() {
        return types.toString();
    }

    @Override
    public String debug() {
        return "\u3008" + stream().map(Debuggable::debug).collect(Collectors.joining(", ")) + "\u3009";
    }
}
