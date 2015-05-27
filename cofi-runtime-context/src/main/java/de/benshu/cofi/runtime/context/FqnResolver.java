package de.benshu.cofi.runtime.context;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.runtime.AbstractObject;
import de.benshu.cofi.runtime.AbstractTypeDeclaration;
import de.benshu.cofi.runtime.MemberDeclaration;
import de.benshu.cofi.runtime.Module;
import de.benshu.cofi.runtime.NamedEntity;
import de.benshu.cofi.runtime.ObjectSingleton;
import de.benshu.cofi.runtime.Package;

import java.util.Objects;
import java.util.function.Supplier;

public class FqnResolver {
    private final Supplier<Module> module;

    public FqnResolver(Supplier<Module> module) {
        this.module = module;
    }

    public NamedEntity resolve(String... fqn) {
        return resolve(Fqn.from(fqn));
    }

    public NamedEntity resolve(Fqn fqn) {
        return module.get().getFqn().contains(fqn)
                ? resolveRelativeNameInPackage(module.get().getPakkage(), module.get().getFqn().getRelativeNameOf(fqn))
                : glueObjectSingletonFor(fqn);
    }

    private NamedEntity resolveRelativeNameInPackage(Package pkg, ImmutableList<String> relativeName) {
        if (relativeName.isEmpty())
            return pkg;

        final String localName = relativeName.get(0);
        final ImmutableList<String> remaining = relativeName.subList(1, relativeName.size());

        return pkg.getSubpackages().stream()
                .filter(p -> Objects.equals(p.getName(), localName))
                .findAny()
                .map(p -> resolveRelativeNameInPackage(p, remaining))
                .orElseGet(() ->
                        pkg.getTopLevelDeclarations().stream()
                                .filter(d -> Objects.equals(d.getName(), localName))
                                .map(d -> resolveRelativeNameInTypeDeclaration(d, remaining))
                                .findAny().get());
    }

    private NamedEntity resolveRelativeNameInTypeDeclaration(AbstractTypeDeclaration typeDeclaration, ImmutableList<String> relativeName) {
        if (relativeName.isEmpty())
            return typeDeclaration;

        final String localName = relativeName.get(0);
        final ImmutableList<String> remaining = relativeName.subList(1, relativeName.size());

        final AbstractObject object = typeDeclaration.getCompanion()
                .map(AbstractObject.class::cast)
                .getOrSupply(() -> (ObjectSingleton) typeDeclaration);

        return object.getBody().getElements().stream()
                .filter(AbstractTypeDeclaration.class::isInstance)
                .map(AbstractTypeDeclaration.class::cast)
                .filter(d -> Objects.equals(d.getName(), localName))
                .map(d -> resolveRelativeNameInTypeDeclaration(d, remaining))
                .findAny().get();
    }

    private ObjectSingleton glueObjectSingletonFor(Fqn fqn) {
        ObjectSingleton current = module.get().getRoot();

        for (String n : fqn)
            current = (ObjectSingleton) current.getType().applyTrivially().lookupMember(n).get()
                    .getTags().get(MemberDeclaration.TAG);

        return current;
    }
}
