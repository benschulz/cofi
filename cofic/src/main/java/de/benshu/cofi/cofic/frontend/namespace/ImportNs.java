package de.benshu.cofi.cofic.frontend.namespace;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.notes.Source;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.ImportStatement;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.streams.Collectors.list;

class ImportNs extends AbstractNamespace {
    public static AbstractNamespace wrap(AbstractNamespace parent, ImportStatement<Pass> imprt) {
        return new ImportNs(parent, imprt);
    }

    private final ImportStatement<Pass> imprt;

    private ImportNs(AbstractNamespace parent, ImportStatement<Pass> imprt) {
        super(parent);

        this.imprt = imprt;
    }

    @Override
    protected Optional<AbstractNamespace> tryResolveNamespaceLocally(LookUp lookUp, String name, Source.Snippet src) {
        return containerNs(lookUp, name)
                .flatMap(ns -> ns.tryResolveNamespaceLocally(lookUp, name, src));
    }

    @Override
    protected Optional<AbstractResolution> tryResolveLocally(LookUp lookUp, AbstractNamespace fromNamespace, String name) {
        return containerNs(lookUp, name)
                .flatMap(ns -> ns.tryResolveLocally(lookUp, fromNamespace, name));
    }

    private Optional<QualifiedTypeNs> containerNs(LookUp lookUp, String name) {
        if (!imprt.name.ids.get(imprt.name.ids.size() - 1).getLexeme().equals(name))
            return none();

        final ImmutableList<String> importIds = imprt.name.ids.stream().map(Token::getLexeme).collect(list());
        final Fqn containerFqn = Fqn.from(importIds.subList(0, importIds.size() - 1));
        return lookUp.tryResolveQualifiedTypeName(containerFqn)
                .map(t -> QualifiedTypeNs.create(containerFqn, t));
    }
}
