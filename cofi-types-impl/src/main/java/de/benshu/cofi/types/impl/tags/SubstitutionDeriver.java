package de.benshu.cofi.types.impl.tags;

import de.benshu.cofi.types.tags.DerivableTag;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.SubstitutionDerivableTag;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.none;

class SubstitutionDeriver extends HashTags.Deriver {
    private final Tags unsubstituted;

    SubstitutionDeriver(Tags unsubstituted) {
        this.unsubstituted = unsubstituted;
    }

    @Override
    public <T> Optional<T> tryDeriveIndirectly(HashTags tags, DerivableTag<T> tag) {
        return tag instanceof SubstitutionDerivableTag<?>
                ? ((SubstitutionDerivableTag<T>) tag).tryDeriveFromSubstitution(unsubstituted, tags)
                : none();
    }
}