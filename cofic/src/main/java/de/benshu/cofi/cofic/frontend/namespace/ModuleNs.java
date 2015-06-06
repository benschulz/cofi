package de.benshu.cofi.cofic.frontend.namespace;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.notes.Source;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.commons.core.Optional;

class ModuleNs extends AbstractNamespace {
    public static ModuleNs wrap(AbstractNamespace parent, CompilationUnit.ModuleDeclaration<Pass> moduleDeclaration) {
        return new ModuleNs(parent, moduleDeclaration.name.fqn);
    }

    private final Fqn moduleFqn;

    public ModuleNs(AbstractNamespace parent, Fqn moduleFqn) {
        super(parent);

        this.moduleFqn = moduleFqn;
    }

    @Override
    protected Optional<AbstractNamespace> tryResolveNamespaceLocally(LookUp lookUp, String name, Source.Snippet src) {
        return cofiLangNs(lookUp).tryResolveNamespaceLocally(lookUp, name, src);
    }

    @Override
    protected Optional<AbstractResolution> tryResolveLocally(LookUp lookUp, AbstractNamespace fromNamespace, String name) {
        return cofiLangNs(lookUp).tryResolveLocally(lookUp, fromNamespace, name);
    }

    private CofiLangNs cofiLangNs(LookUp lookUp) {
        return CofiLangNs.create(ImmutableList.of(), lookUp.tryLookUpLangType(ImmutableList.of()).get());
    }
}
