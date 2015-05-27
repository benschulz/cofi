package de.benshu.cofi.types.impl.tags;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.types.tags.DerivableTag;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.UnionDerivableTag;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.none;

class UnionDeriver extends HashTags.Deriver {
    private final ImmutableSet<Tags> elements;

    UnionDeriver(ImmutableSet<Tags> elements) {
        this.elements = elements;
    }

    @Override
    public <T> Optional<T> tryDeriveIndirectly(HashTags tags, DerivableTag<T> tag) {
        return tag instanceof UnionDerivableTag<?>
                ? ((UnionDerivableTag<T>) tag).tryDeriveFromUnion(elements, tags)
                : none();
    }
}