package de.benshu.cofi.types.tags;

import de.benshu.commons.core.Optional;

public interface RefinementDerivableTag<T> extends DerivableTag<T> {
    Optional<T> tryDeriveFromRefinement(Tags unrefined, IndividualTags refinement, Tags refined);
}
