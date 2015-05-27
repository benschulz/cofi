package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.commons.core.Optional;
import de.benshu.cofi.cofic.notes.Source;

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
    protected Optional<AbstractNamespace> tryResolveNamespaceLocally(String name, Source.Snippet src) {
        return cofiLangNs().tryResolveNamespaceLocally(name, src);
    }

    @Override
    protected Optional<AbstractResolution> tryResolveLocally(AbstractNamespace fromNamespace, String name) {
        return cofiLangNs().tryResolveLocally(fromNamespace, name);
    }

    private PackageObjectNs cofiLangNs() {
        final Fqn cofiLangFqn = Fqn.from("cofi", "lang");
        return PackageObjectNs.create(this, cofiLangFqn, pass.lookUpPackageObjectDeclarationOf(cofiLangFqn));
    }
}
