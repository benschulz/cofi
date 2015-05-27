package de.benshu.cofi.types.impl.declarations;

import de.benshu.cofi.cofic.notes.async.Checker;

public interface Interpreter<I, O> {
    static <T> Interpreter<T, T> id() {
        return (input, checker) -> input;
    }

    O interpret(I input, Checker checker);
}
