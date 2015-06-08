package de.benshu.cofi.runtime.context;

import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.runtime.Companion;
import de.benshu.cofi.runtime.MemberDeclaration;
import de.benshu.cofi.runtime.Module;
import de.benshu.cofi.runtime.NamedEntity;

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
        NamedEntity current = module.get().getRoot();

        for (String n : fqn)
            current = (NamedEntity) current.getType().applyTrivially().lookupMember(n).get()
                    .getTags().get(MemberDeclaration.TAG);

        return current instanceof Companion
                ? ((Companion) current).getAccompanied()
                : current;
    }
}
