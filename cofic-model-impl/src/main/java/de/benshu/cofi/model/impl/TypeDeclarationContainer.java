package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableListMultimap;

import java.util.stream.Stream;

import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.commons.core.streams.Collectors.listMultimap;

public interface TypeDeclarationContainer<X extends ModelContext<X>> {

    Stream<AbstractTypeDeclaration<X>> getTypeDeclarations();

    default ImmutableListMultimap<String, ? extends AbstractTypeDeclaration<X>> getTypeDeclarationsByName() {
        return getTypeDeclarations()
                .map(d -> immutableEntry(d.getName(), d))
                .collect(listMultimap());

    }
}
