package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.notes.Source;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AbstractModuleOrPackageObjectDeclaration;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.commons.core.Optional;

class PackageNs extends AbstractNamespace {
    public static AbstractNamespace wrap(AbstractNamespace parent, Fqn packageFqn, AbstractModuleOrPackageObjectDeclaration<Pass> declaration) {
        return new PackageNs(parent, packageFqn, declaration);
    }

    private final Fqn packageFqn;
    private final AbstractModuleOrPackageObjectDeclaration<Pass> declaration;

    PackageNs(AbstractNamespace parent, Fqn packageFqn, AbstractModuleOrPackageObjectDeclaration<Pass> declaration) {
        super(parent);

        this.packageFqn = packageFqn;
        this.declaration = declaration;
    }

    @Override
    public AbstractTypeDeclaration<Pass> getContainingTypeDeclaration() {
        return declaration;
    }

    @Override
    public Fqn getPackageFqn() {
        return packageFqn;
    }

    @Override
    public Fqn getContainingEntityFqn() {
        return packageFqn;
    }

    @Override
    protected Optional<AbstractNamespace> tryResolveNamespaceLocally(LookUp lookUp, String name, Source.Snippet src) {
        return packageObjectNs().tryResolveNamespaceLocally(lookUp, name, src);
    }

    @Override
    protected Optional<AbstractResolution> tryResolveLocally(LookUp lookUp, AbstractNamespace fromNamespace, String name) {
        return packageObjectNs().tryResolveLocally(lookUp, fromNamespace, name);
    }

    private ModuleOrPackageObjectNs packageObjectNs() {
        return ModuleOrPackageObjectNs.create(this, packageFqn, declaration);
    }
}
