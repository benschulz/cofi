package de.benshu.cofi.model.impl;

import de.benshu.cofi.model.Module.Version;

public class ModuleImpl<X extends ModelContext<X>> {
    public static <X extends ModelContext<X>> ModuleImpl<X> create(Version version) {
        return new ModuleImpl<>(version);
    }

    private final Version version;

    private ModuleImpl(Version version) {
        this.version = version;
    }
}
