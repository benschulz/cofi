package de.benshu.cofi.model.impl;

import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.Module.Version;
import de.benshu.cofi.types.impl.TypeSystemImpl;

public class ModuleImpl<X extends ModelContext<X>> {
    public static <X extends ModelContext<X>> ModuleImpl<X> create(X context, Fqn fqn, Version version) {
        return new ModuleImpl<>(context, null, fqn, version);
    }

    public static <X extends ModelContext<X>> ModuleImpl<X> create(X context, ModuleImpl<X> langModule, Fqn fqn, Version version) {
        return new ModuleImpl<>(context, langModule, fqn, version);
    }

    private final Fqn fqn;
    private final Version version;
    private final TypeSystemImpl<X> types;
    private ModuleImpl<X> langModule;

    private ModuleImpl(X context, ModuleImpl<X> langModule, Fqn fqn, Version version) {
        this.fqn = fqn;
        this.version = version;
        this.types = context.getTypeSystem();
        this.langModule = langModule == null ? this : langModule;
    }

    public TypeSystemImpl<X> getTypeSystem() {
        return types;
    }

    public Fqn getFullyQualifiedName() {
        return fqn;
    }

    public ModuleImpl<X> getLangModule() {
        return langModule;
    }
}
