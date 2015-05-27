package de.benshu.cofi.runtime.context;

import de.benshu.cofi.types.tags.IndividualTag;
import de.benshu.cofi.types.tags.SimpleDefaultingIndividualTag;
import de.benshu.commons.core.Debuggable;

public class RuntimeTypeName implements Debuggable {
    public static final SimpleDefaultingIndividualTag<RuntimeTypeName> TAG = IndividualTag.named("Name").defaultingTo(RuntimeTypeName.of("<anonymous>"));

    public static RuntimeTypeName of(String name) {
        return new RuntimeTypeName(name);
    }

    private final String name;

    private RuntimeTypeName(String name) {
        this.name = name;
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
