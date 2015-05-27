package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.Kind;

import static com.google.common.base.Preconditions.checkArgument;

public class KindImpl implements Kind {
    public static final KindImpl PROPER_ORDER = new KindImpl(0);
    public static final KindImpl FIRST_ORDER = new KindImpl(1);

    private final int order;

    public KindImpl(int order) {
        checkArgument(order >= 0);
        this.order = order;
    }

    @Override
    public boolean isHigherOrder() {
        return order > 1;
    }

    @Override
    public boolean isFirstOrder() {
        return order == 1;
    }

    @Override
    public boolean isProperOrder() {
        return order == 0;
    }
}
