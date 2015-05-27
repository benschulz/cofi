package de.benshu.cofi.interpreter.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.runtime.VariableDeclaration;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.benshu.commons.core.streams.Collectors.list;

public class EvaluationContext {
    private final CofiObject owner;
    private final ImmutableList<ImmutableMap<VariableDeclaration, CofiObject>> lexicalScope;

    public EvaluationContext(CofiObject owner, ImmutableList<ImmutableMap<VariableDeclaration, CofiObject>> lexicalScope) {
        this.owner = owner;
        this.lexicalScope = lexicalScope;
    }

    public EvaluationContext(CofiObject owner) {
        this(owner, ImmutableList.of());
    }

    public EvaluationContext narrow(ImmutableList<ImmutableMap<VariableDeclaration, CofiObject>> furtherContext) {
        return new EvaluationContext(getOwner(), Stream.concat(lexicalScope.stream(), furtherContext.stream()).collect(list()));
    }

    public CofiObject getOwner() {
        return owner;
    }

    public VariableDeclaration lookUpVariable(String name) {
        return lookUpVariableEntry(v -> v.getName().equals(name)).getKey();
    }

    public CofiObject get(VariableDeclaration variable) {
        return lookUpVariableEntry(variable::equals).getValue();
    }

    public Map.Entry<VariableDeclaration, CofiObject> lookUpVariableEntry(Predicate<VariableDeclaration> predicate) {
        return lexicalScope.reverse().stream().flatMap(vs -> vs.entrySet().stream())
                .filter(e -> predicate.test(e.getKey()))
                .findFirst().get();
    }

    public void put(VariableDeclaration variable, CofiObject value) {
        for (Map<VariableDeclaration, CofiObject> vars : lexicalScope.reverse()) {
            final CofiObject instance = vars.get(variable);
            if (instance != null) {
                instance.getSpecialValues().putInstance(CofiObject.class, value);
                return;
            }
        }

        throw new AssertionError();
    }
}
