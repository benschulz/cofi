package de.benshu.cofi.types.impl.tags;

import de.benshu.cofi.types.tags.ApplicationDerivableTag;
import de.benshu.cofi.types.tags.DerivableTag;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Optional;

class ApplicationDeriver extends HashTags.Deriver {
    private final Tags unapplied;

    ApplicationDeriver(Tags unapplied) {
        this.unapplied = unapplied;
    }

    @Override
    public <T> Optional<T> tryDeriveIndirectly(HashTags tags, DerivableTag<T> tag) {
        return tag instanceof ApplicationDerivableTag<?>
                ? ((ApplicationDerivableTag<T>) tag).tryDeriveFromApplication(unapplied, tags)
                : Optional.<T>none();
    }
}