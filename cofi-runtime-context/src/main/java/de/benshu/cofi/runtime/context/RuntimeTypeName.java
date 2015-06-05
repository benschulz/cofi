package de.benshu.cofi.runtime.context;

import de.benshu.cofi.types.impl.TypeName;
import de.benshu.cofi.types.tags.IndividualTag;
import de.benshu.cofi.types.tags.SimpleDefaultingIndividualTag;

public class RuntimeTypeName implements TypeName {
    public static final SimpleDefaultingIndividualTag<RuntimeTypeName> TAG = IndividualTag.named("Name").defaultingTo(RuntimeTypeName.of("<anonymous>"));

    public static RuntimeTypeName of(String name) {
        return new RuntimeTypeName(name);
    }

    private final String name;

    private RuntimeTypeName(String name) {
        this.name = name;
    }

    @Override
    public String toDescriptor() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String debug() {
        return name;
    }
}
