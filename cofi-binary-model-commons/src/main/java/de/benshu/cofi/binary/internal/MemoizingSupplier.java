package de.benshu.cofi.binary.internal;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public class MemoizingSupplier<T> implements Supplier<T> {
    public static <T> MemoizingSupplier<T> of(Supplier<T> delegate) {
        return new MemoizingSupplier<>(delegate);
    }

    private Supplier<T> delegate;

    private volatile T value;

    public MemoizingSupplier(Supplier<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T get() {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = checkNotNull(delegate.get());
                    delegate = null;
                }
            }
        }

        return value;
    }
}
