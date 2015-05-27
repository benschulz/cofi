package de.benshu.cofi.types.impl.tags;

import de.benshu.cofi.types.tags.DerivableTag;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.RefinementDerivableTag;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.none;

class RefinementDeriver extends HashTags.Deriver {
    private final Tags unrefined;
    private final IndividualTags refinement;

    public RefinementDeriver(Tags unrefined, IndividualTags refinement) {
        this.unrefined = unrefined;
        this.refinement = refinement;
    }

    @Override
    public <T> Optional<T> tryDeriveIndirectly(HashTags tags, DerivableTag<T> tag) {
        return tag instanceof RefinementDerivableTag<?>
                ? ((RefinementDerivableTag<T>) tag).tryDeriveFromRefinement(unrefined, refinement, tags)
                : none();
    }
}