package de.benshu.cofi.binary.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import de.benshu.commons.core.Optional;

import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Iterators.singletonIterator;
import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;
import static de.benshu.commons.core.streams.Collectors.list;
import static de.benshu.commons.core.streams.Collectors.set;

public abstract class Ancestry implements Iterable<Object> {
    private static final Nil EMPTY = new Nil();

    public static Ancestry empty() {
        return EMPTY;
    }

    public static <N> Ancestry first(N adamOrEve) {
        return empty().append(adamOrEve);
    }

    private Ancestry() {}

    public Stream<Object> ancestors() {
        return StreamSupport.stream(spliterator(), false);
    }

    public Ancestry append(Object child) {
        return new Head<>(this, child);
    }

    public <S> Optional<S> closest(Class<S> ancestorType) {
        return beginningWith(ancestorType).map(Head::getParent);
    }

    public abstract <S> Optional<Head<S>> beginningWith(Class<S> ancestorType);

    public <T> T construct(Constructor<T> constructor) {
        return constructor.construct(this);
    }

    public <T> ImmutableList<T> constructAll(ImmutableList<Constructor<T>> constructors) {
        return constructors.stream().map(this::construct).collect(list());
    }

    public <T> ImmutableSet<T> constructAll(ImmutableSet<Constructor<T>> constructors) {
        return constructors.stream().map(this::construct).collect(set());
    }

    public static class Head<N> extends Ancestry {
        private final Ancestry grandAncestry;
        private final N parent;

        private Head(Ancestry grandAncestry, N parent) {
            this.grandAncestry = grandAncestry;
            this.parent = parent;
        }

        public N getParent() {
            return parent;
        }

        public Ancestry getGrandAncestry() {
            return grandAncestry;
        }

        @Override
        public <T> Optional<Head<T>> beginningWith(Class<T> ancestorType) {
            return ancestorType.isInstance(parent)
                    ? some(new Head<>(grandAncestry, ancestorType.cast(parent)))
                    : grandAncestry.beginningWith(ancestorType);
        }

        @Override
        public Iterator<Object> iterator() {
            return Iterators.concat(singletonIterator(parent), grandAncestry.iterator());
        }
    }

    private static class Nil extends Ancestry {
        @Override
        public Iterator<Object> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public <T> Optional<Head<T>> beginningWith(Class<T> ancestorType) {
            return none();
        }
    }
}
