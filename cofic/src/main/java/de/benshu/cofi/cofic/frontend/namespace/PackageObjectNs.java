package de.benshu.cofi.cofic.frontend.namespace;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.notes.Source;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.model.impl.ObjectDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;

import java.util.Optional;

import static de.benshu.commons.core.Optional.from;
import static de.benshu.commons.core.Optional.some;

public class PackageObjectNs extends AbstractNamespace {
    static PackageObjectNs wrap(AbstractNamespace parent,
                                Fqn packageFqn,
                                PackageObjectDeclaration<Pass> packageObjectDeclaration) {
        return new PackageObjectNs(parent, packageFqn, packageObjectDeclaration);
    }

    static PackageObjectNs create(AbstractNamespace parent,
                                  Fqn packageFqn,
                                  PackageObjectDeclaration<Pass> packageObjectDeclaration) {
        return new PackageObjectNs(parent, packageFqn, packageObjectDeclaration);
    }

    private final Fqn packageFqn;
    private final PackageObjectDeclaration<Pass> packageObjectDeclaration;

    private PackageObjectNs(AbstractNamespace parent,
                            Fqn packageFqn,
                            PackageObjectDeclaration<Pass> packageObjectDeclaration) {
        super(parent);

        this.packageFqn = packageFqn;
        this.packageObjectDeclaration = packageObjectDeclaration;
    }

    @Override
    ExpressionNode<Pass> getAccessor() {
        return getAccessor(packageFqn);
    }

    @Override
    public AbstractTypeDeclaration<Pass> getContainingTypeDeclaration() {
        return packageObjectDeclaration;
    }

    @Override
    protected de.benshu.commons.core.Optional<AbstractNamespace> tryResolveNamespaceLocally(String name, Source.Snippet src) {
        final Fqn childFqn = packageFqn.getChild(name);

        for (PackageObjectDeclaration<Pass> packageObjectDeclaration : pass.tryLookUpPackageObjectDeclarationOf(childFqn))
            return some(PackageObjectNs.create(this, childFqn, packageObjectDeclaration));

        final ImmutableSet<AbstractTypeDeclaration<Pass>> tlds = pass.lookUpTopLevelDeclarationIn(packageObjectDeclaration);

        final Optional<AbstractTypeDeclaration<Pass>> tld = tlds.stream()
                .filter(d -> d.getName().equals(name))
                .sorted((a, b) -> a instanceof ObjectDeclaration<?> ? 1 : -1)
                .findFirst();

        return from(tld.map(d -> TypeDeclarationNs.outOfScope(this, d)));
    }

    @Override
    protected de.benshu.commons.core.Optional<AbstractResolution> tryResolveLocally(AbstractNamespace fromNamespace, String name) {
        return pass.lookUpTypeOf(packageObjectDeclaration).applyTrivially().lookupMember(name)
                .map(m -> new DefaultResolution(m.getType(), getAccessor(), m));
    }
}
