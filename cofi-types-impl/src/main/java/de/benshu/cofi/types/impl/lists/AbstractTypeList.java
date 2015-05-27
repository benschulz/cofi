package de.benshu.cofi.types.impl.lists;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import de.benshu.cofi.types.Type;
import de.benshu.cofi.types.bound.TypeList;
import de.benshu.cofi.types.impl.AbstractUnboundType;
import de.benshu.cofi.types.impl.SomeTypeSystemContext;
import de.benshu.cofi.types.impl.Substitutable;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Collector;

import static de.benshu.cofi.types.impl.lists.UnboundTypeList.unboundTypeList;
import static de.benshu.commons.core.streams.Collectors.list;
import static java.util.stream.Collectors.collectingAndThen;

public abstract class AbstractTypeList<X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> implements TypeList<X, T> {
    public static <X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> Collector<T, ?, AbstractTypeList<X, T>> typeList() {
        return collectingAndThen(list(), AbstractTypeList::of);
    }

    private static final AbstractTypeList<?, ?> EMPTY = AbstractTypeList.<SomeTypeSystemContext, TypeMixin<SomeTypeSystemContext, ?>>of(ImmutableList.of());

    private final ImmutableList<T> types;

    AbstractTypeList(ImmutableList<? extends T> types) {
        this.types = ImmutableList.copyOf(types);
    }

    public static <X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> AbstractTypeList<X, T> of(ImmutableList<? extends T> types) {
        return new RegularTypeList<>(types);
    }

    public static <X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> AbstractTypeList<X, T> of(T type) {
        return of(ImmutableList.of(type));
    }

    public static <X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> AbstractTypeList<X, T> of(T type1, T type2) {
        return of(ImmutableList.of(type1, type2));
    }

    public static <X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> AbstractTypeList<X, T> of(T type1, T type2, T type3) {
        return of(ImmutableList.of(type1, type2, type3));
    }

    public static <X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> AbstractTypeList<X, T> of(T[] types) {
        return of(ImmutableList.copyOf(types));
    }

    public static <X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> AbstractTypeList<X, T> of(Iterable<? extends T> types) {
        return of(ImmutableList.copyOf(types));
    }

    @SuppressWarnings("unchecked")
    public static <X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> AbstractTypeList<X, T> empty() {
        return (RegularTypeList<X, T>) EMPTY;
    }

    public static <X extends TypeSystemContext<X>> AbstractTypeList<X, ?> rebind(de.benshu.cofi.types.TypeList<?> unbound) {
        return unbound.stream()
                .map(b -> ((AbstractUnboundType<?, ?>) b).<X>rebind().bound)
                .collect(typeList());
    }

    public T get(int index) {
        return types.get(index);
    }

    public int size() {
        return types.size();
    }

    // TODO why ImmutableList
    public <A> ImmutableList<A> mapAny(Function<? super T, ? extends A> f) {
        return ImmutableList.copyOf(types.stream().map(f).iterator());
    }

    public <U extends TypeMixin<X, ?>> AbstractTypeList<X, U> map(Function<? super T, ? extends U> f) {
        return new RegularTypeList<>(mapAny(f));
    }

    /* we want to generify: <U super T, U extends Substitutable<X, S>, S extends TypeMixin<X, ?>> */
    public AbstractTypeList<X, ?> substitute(Substitutions<X> substitutions) {
        return map(e -> e.substitute(substitutions));
    }

    // we need declaration site variance and super bounds to make it sound.. (see above)
    public <U extends TypeMixin<X, ?> & Substitutable<X, S>, S extends TypeMixin<X, ?>> AbstractTypeList<X, S> substituteUnchecked(Substitutions<X> substitutions) {
        return map(e -> ((U) e).substitute(substitutions));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        return types.iterator();
    }

    public String debug() {
        return "\u3008" + Joiner.on(", ").join(mapAny(a -> a.debug())) + "\u3009";
    }

    public String toDescriptor() {
        return CharMatcher.WHITESPACE.removeFrom(debug());
    }

    public AbstractTypeList<X, T> subList(int fromIndex, int toIndex) {
        return new RegularTypeList<>(types.subList(fromIndex, toIndex));
    }

    public AbstractTypeList<X, T> append(T type) {
        return of(Iterables.concat(types, ImmutableList.of(type)));
    }

    public boolean isEmpty() {
        return size() < 1;
    }

    @Override
    public String toString() {
        return types.toString();
    }

    // TODO need another type parameter <U | T <: U <: Unbindable<B>>
    public <B extends Type> de.benshu.cofi.types.TypeList<B> unbind() {
        return stream().map(e -> (B) e.unbind()).collect(unboundTypeList());
    }
}
