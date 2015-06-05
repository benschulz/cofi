package de.benshu.cofi.model.impl;

import de.benshu.cofi.types.impl.TypeName;

public class LocalTypeName implements TypeName {
    public static LocalTypeName create(String name) {
        return new LocalTypeName(name);
    }

    private final String name;

    private LocalTypeName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return debug();
    }

    @Override
    public String debug() {
        return name;
    }

    @Override
    public String toDescriptor() {
        return name;
    }
}
