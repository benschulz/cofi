package de.benshu.cofi.cofic.frontend.namespace;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.notes.Source;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AbstractModuleOrPackageObjectDeclaration;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.model.impl.ObjectDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;

import java.util.Optional;

import static de.benshu.commons.core.Optional.from;
import static de.benshu.commons.core.Optional.some;

public class ModuleOrPackageObjectNs extends AbstractNamespace {
    static ModuleOrPackageObjectNs wrap(AbstractNamespace parent,
                                        Fqn packageFqn,
                                        AbstractModuleOrPackageObjectDeclaration<Pass> declaration) {
        return new ModuleOrPackageObjectNs(parent, packageFqn, declaration);
    }

    static ModuleOrPackageObjectNs create(AbstractNamespace parent,
                                          Fqn packageFqn,
                                          AbstractModuleOrPackageObjectDeclaration<Pass> declaration) {
        return new ModuleOrPackageObjectNs(parent, packageFqn, declaration);
    }

    private final Fqn packageFqn;
    private final AbstractModuleOrPackageObjectDeclaration<Pass> declaration;

    private ModuleOrPackageObjectNs(
            AbstractNamespace parent,
            Fqn packageFqn,
            AbstractModuleOrPackageObjectDeclaration<Pass> declaration) {
        super(parent);

        this.packageFqn = packageFqn;
        this.declaration = declaration;
    }

    @Override
    ExpressionNode<Pass> getAccessor(LookUp lookUp) {
        return getAccessor(packageFqn);
    }

    @Override
    public AbstractTypeDeclaration<Pass> getContainingTypeDeclaration() {
        return declaration;
    }

    @Override
    protected de.benshu.commons.core.Optional<AbstractNamespace> tryResolveNamespaceLocally(LookUp lookUp, String name, Source.Snippet src) {
        final Fqn childFqn = packageFqn.getChild(name);

        for (PackageObjectDeclaration<Pass> packageObjectDeclaration : lookUp.tryLookUpPackageObjectDeclarationOf(childFqn))
            return some(ModuleOrPackageObjectNs.create(this, childFqn, packageObjectDeclaration));

        final ImmutableSet<AbstractTypeDeclaration<Pass>> tlds = lookUp.lookUpTopLevelDeclarationIn(packageFqn);

        final Optional<AbstractTypeDeclaration<Pass>> tld = tlds.stream()
                .filter(d -> d.getName().equals(name))
                .sorted((a, b) -> a instanceof ObjectDeclaration<?> ? 1 : -1)
                .findFirst();

        return from(tld.map(d -> TypeDeclarationNs.outOfScope(this, d)));
    }

    @Override
    protected de.benshu.commons.core.Optional<AbstractResolution> tryResolveLocally(LookUp lookUp, AbstractNamespace fromNamespace, String name) {
        return lookUp.lookUpTypeOf(declaration).applyTrivially().lookupMember(name)
                .map(m -> new DefaultResolution(m.getType(), getAccessor(lookUp), m));
    }
}
