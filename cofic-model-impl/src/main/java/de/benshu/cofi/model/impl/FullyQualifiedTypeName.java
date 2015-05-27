package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.model.TypeName;

import static java.util.stream.Collectors.joining;

public class FullyQualifiedTypeName implements TypeName {
    public static FullyQualifiedTypeName create(ImmutableList<String> fqn, String postfix) {
        return new FullyQualifiedTypeName(fqn, postfix);
    }

    public static FullyQualifiedTypeName create(ImmutableList<String> fqn) {
        return create(fqn, "");
    }

    private final ImmutableList<String> fqn;
    private final String postfix;

    private FullyQualifiedTypeName(ImmutableList<String> fqn, String postfix) {
        this.fqn = fqn;
        this.postfix = postfix;
    }

    public ImmutableList<String> getFullyQualifiedName() {
        return fqn;
    }

    @Override
    public String toString() {
        return debug();
    }

    @Override
    public String debug() {
        return "." + fqn.stream().collect(joining(".")) + postfix;
    }
}
