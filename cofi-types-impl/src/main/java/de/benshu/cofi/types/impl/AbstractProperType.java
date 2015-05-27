package de.benshu.cofi.types.impl;

public abstract class AbstractProperType<X extends TypeSystemContext<X>, S extends AbstractProperType<X, S> & ProperTypeMixin<X, S>>
        extends AbstractType<X, S>
        implements ProperTypeMixin<X, S> {

    protected AbstractProperType(X context) {
        super(context);
    }
}
