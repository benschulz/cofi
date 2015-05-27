package de.benshu.cofi.interpreter;

import de.benshu.cofi.interpreter.internal.ModuleInterpretation;
import de.benshu.cofi.runtime.Module;
import de.benshu.cofi.runtime.context.RuntimeContext;

public class CofiInterpreter {
    private final RuntimeContext context;

    public CofiInterpreter(RuntimeContext context) {
        this.context = context;
    }

    public void start(Module module) {
        new ModuleInterpretation(context, module).perform();
    }
}
