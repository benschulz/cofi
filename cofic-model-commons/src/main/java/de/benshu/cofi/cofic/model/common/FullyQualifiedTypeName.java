package de.benshu.cofi.cofic.model.common;

import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.impl.TypeName;

import java.util.function.Supplier;

import static java.util.stream.Collectors.joining;

public class FullyQualifiedTypeName implements TypeName {
    public static FullyQualifiedTypeName create(Fqn fqn, String postfix) {
        return create(() -> fqn, postfix);
    }

    public static FullyQualifiedTypeName create(Fqn fqn) {
        return create(fqn, "");
    }

    public static FullyQualifiedTypeName create(Supplier<Fqn> fqn, String postfix) {
        return new FullyQualifiedTypeName(fqn, postfix);
    }

    public static FullyQualifiedTypeName create(Supplier<Fqn> fqn) {
        return create(fqn, "");
    }

    private final Supplier<Fqn> fqn;
    private final String postfix;

    private FullyQualifiedTypeName(Supplier<Fqn> fqn, String postfix) {
        this.fqn = fqn;
        this.postfix = postfix;
    }

    @Override
    public String toString() {
        return debug();
    }

    @Override
    public String debug() {
        return "." + fqn.get().components().collect(joining(".")) + postfix;
    }

    @Override
    public String toDescriptor() {
        return "." + fqn.get().components().collect(joining("."));
    }
}
