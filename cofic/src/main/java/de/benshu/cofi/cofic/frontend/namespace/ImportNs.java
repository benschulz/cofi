package de.benshu.cofi.cofic.frontend.namespace;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.ImportStatement;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.commons.core.Optional;
import de.benshu.cofi.cofic.notes.Source;

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
    protected Optional<AbstractNamespace> tryResolveNamespaceLocally(String name, Source.Snippet src) {
        if (imprt.name.ids.get(imprt.name.ids.size() - 1).getLexeme().equals(name)) {
            final ImmutableList<String> importIds = imprt.name.ids.stream().map(Token::getLexeme).collect(list());
            return getRoot().tryResolveNamespace(importIds, imprt.name.getSourceSnippet());
        }

        return none();
    }

    @Override
    protected Optional<AbstractResolution> tryResolveLocally(AbstractNamespace fromNamespace, String name) {
        if (imprt.name.ids.get(imprt.name.ids.size() - 1).getLexeme().equals(name)) {
            final ImmutableList<String> importIds = imprt.name.ids.stream().map(Token::getLexeme).collect(list());
            return getRoot().tryResolveNamespace(importIds.subList(0, importIds.size() - 1), imprt.name.getSourceSnippet())
                    .flatMap(n -> n.tryResolveLocally(fromNamespace, name));
        }

        return none();
    }
}
