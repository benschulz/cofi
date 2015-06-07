package de.benshu.cofi.common;

import com.google.common.base.Joiner;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import de.benshu.commons.core.streams.Collectors;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public class Fqn implements Comparable<Fqn>, Iterable<String> {
    public static Fqn root() {
        return from();
    }

    public static Fqn from(String... ids) {
        return from(ImmutableList.copyOf(ids));
    }

    public static Fqn from(ImmutableList<String> ids) {
        return new Fqn(ids.toArray(new String[ids.size()]));
    }

    private final String[] ids;

    private Fqn(String[] ids) {
        this.ids = ids;
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof Fqn && equals((Fqn) o);
    }

    public boolean equals(Fqn other) {
        return Arrays.equals(other.ids, ids);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ids);
    }

    @Override
    public String toString() {
        return toCanonicalString();
    }

    public String getLocalName() {
        return ids[ids.length - 1];
    }

    public boolean contains(Fqn other) {
        return length() <= other.length() && containsInternal(other);
    }

    public boolean strictlyContains(Fqn other) {
        return length() < other.length() && containsInternal(other);
    }

    public boolean containsInternal(Fqn other) {
        return equals(other.getAncestor(length()));
    }

    public ImmutableList<String> getRelativeNameOf(Fqn descendant) {
        checkArgument(contains(descendant));
        return FluentIterable.of(descendant.ids).skip(length()).toList();
    }

    public Fqn getParent() {
        return getAncestor(length() - 1);
    }

    public ImmutableSet<Fqn> getAncestry() {
        return ContiguousSet.create(Range.closed(0, length()), DiscreteDomain.integers())
                .stream().map(this::getAncestor)
                .collect(Collectors.set());
    }

    private Fqn getAncestor(int length) {
        checkArgument(length <= ids.length);
        return new Fqn(Arrays.copyOf(ids, length));
    }

    public Fqn getChild(String localName) {
        return getDescendant(localName);
    }

    public Fqn getDescendant(ImmutableList<String> names) {
        return getDescendant(names.toArray(new String[names.size()]));
    }

    public Fqn getDescendant(String... names) {
        final int length = length();
        String[] descendantIds = Arrays.copyOf(ids, length + names.length);
        System.arraycopy(names, 0, descendantIds, ids.length, names.length);
        return new Fqn(descendantIds);
    }

    public int length() {
        return ids.length;
    }

    public String get(int index) {
        return ids[index];
    }

    @Override
    public int compareTo(Fqn other) {
        final int l = Math.min(length(), other.length());

        for (int i = 0; i < l; ++i) {
            int c = get(i).compareTo(other.get(i));

            if (c != 0)
                return c;
        }

        return Integer.compare(length(), other.length());
    }

    public String toCanonicalString() {
        return "." + Joiner.on(".").join(ids);
    }

    public Stream<String> components() {
        return Stream.of(ids);
    }

    @Override
    public Iterator<String> iterator() {
        return Iterators.forArray(ids);
    }
}
