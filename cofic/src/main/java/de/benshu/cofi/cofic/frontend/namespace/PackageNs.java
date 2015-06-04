package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.commons.core.Optional;
import de.benshu.cofi.cofic.notes.Source;

class PackageNs extends AbstractNamespace {
    public static AbstractNamespace wrap(AbstractNamespace parent, Fqn packageFqn, PackageObjectDeclaration<Pass> declaration) {
        return new PackageNs(parent, packageFqn, declaration);
    }

    private final Fqn packageFqn;
    private final PackageObjectDeclaration<Pass> declaration;

    PackageNs(AbstractNamespace parent, Fqn packageFqn, PackageObjectDeclaration<Pass> declaration) {
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
        return packageObjectNs(lookUp).tryResolveNamespaceLocally(lookUp, name, src);
    }

    @Override
    protected Optional<AbstractResolution> tryResolveLocally(LookUp lookUp, AbstractNamespace fromNamespace, String name) {
        return packageObjectNs(lookUp).tryResolveLocally(lookUp, fromNamespace, name);
    }

    private PackageObjectNs packageObjectNs(LookUp lookUp) {
        return PackageObjectNs.create(this, packageFqn, lookUp.lookUpPackageObjectDeclarationOf(packageFqn));
    }
}
