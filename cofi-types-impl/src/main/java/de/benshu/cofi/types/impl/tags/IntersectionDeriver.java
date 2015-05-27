package de.benshu.cofi.types.impl.tags;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.types.tags.DerivableTag;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IntersectionDerivableTag;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.none;

class IntersectionDeriver extends HashTags.Deriver {
    private final ImmutableSet<Tags> elements;

    public IntersectionDeriver(ImmutableSet<Tags> elements) {
        this.elements = elements;
    }

    @Override
    public <T> Optional<T> tryDeriveIndirectly(HashTags tags, DerivableTag<T> tag) {
        return tag instanceof IntersectionDerivableTag<?>
                ? ((IntersectionDerivableTag<T>) tag).tryDeriveFromIntersection(elements, tags)
                : none();
    }
}