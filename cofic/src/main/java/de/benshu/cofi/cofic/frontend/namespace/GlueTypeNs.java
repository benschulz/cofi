package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.GenericModelDataBuilder;
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

    public GlueTypeNs(Pass pass, GenericModelDataBuilder<?, ?> aggregate) {
        super(pass, aggregate);

        this.fqn = Fqn.from();
    }

    GlueTypeNs(AbstractNamespace parent, Fqn fqn) {
        super(parent);

        this.fqn = fqn;
    }

    @Override
    protected TypeMixin<Pass, ?> asType() {
        return pass.getGlueTypes().get(fqn);
    }

    @Override
    protected Optional<AbstractNamespace> tryResolveNamespaceLocally(String name, Source.Snippet src) {
        final Fqn childFqn = fqn.getChild(name);

        for (PackageObjectDeclaration<Pass> packageObjectDeclaration : pass.tryLookUpPackageObjectDeclarationOf(childFqn))
            return some(PackageObjectNs.create(this, childFqn, packageObjectDeclaration));

        return Optional.from(pass.getGlueTypes().get(childFqn))
                .map(g -> GlueTypeNs.create(this, childFqn));
    }
}
