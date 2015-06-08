package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.notes.Source;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.some;

public class GlueTypeNs extends AbstractNamespace {
    public static GlueTypeNs create(AbstractNamespace parent, Fqn fqn) {
        return new GlueTypeNs(parent, fqn);
    }

    private final Fqn fqn;

    public GlueTypeNs() {
        super();

        this.fqn = Fqn.root();
    }

    GlueTypeNs(AbstractNamespace parent, Fqn fqn) {
        super(parent);

        this.fqn = fqn;
    }

    @Override
    protected TypeMixin<Pass, ?> asType(LookUp lookUp) {
        return lookUp.getGlueTypes().get(fqn);
    }

    @Override
    protected Optional<AbstractNamespace> tryResolveNamespaceLocally(LookUp lookUp, String name, Source.Snippet src) {
        final Fqn childFqn = fqn.getChild(name);

        for (PackageObjectDeclaration<Pass> packageObjectDeclaration : lookUp.tryLookUpPackageObjectDeclarationOf(childFqn))
            return some(ModuleOrPackageObjectNs.create(this, childFqn, packageObjectDeclaration));

        return Optional.from(lookUp.getGlueTypes().get(childFqn))
                .map(g -> GlueTypeNs.create(this, childFqn));
    }
}
