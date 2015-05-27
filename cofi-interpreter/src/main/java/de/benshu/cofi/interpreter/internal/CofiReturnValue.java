package de.benshu.cofi.interpreter.internal;

public class CofiReturnValue extends RuntimeException {
    private final CofiObject value;

    public CofiReturnValue(CofiObject value) {
        this.value = value;
    }

    public CofiObject getValue() {
        return value;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
