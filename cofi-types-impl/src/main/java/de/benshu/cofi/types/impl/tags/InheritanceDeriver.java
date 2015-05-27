package de.benshu.cofi.types.impl.tags;

import de.benshu.cofi.types.tags.DerivableTag;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.InheritanceDerivableTag;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.none;

class InheritanceDeriver extends HashTags.Deriver {
    private final Tags inherited;

    public InheritanceDeriver(Tags inherited) {
        this.inherited = inherited;
    }

    @Override
    public <T> Optional<T> tryDeriveIndirectly(HashTags tags, DerivableTag<T> tag) {
        return tag instanceof InheritanceDerivableTag<?>
                ? ((InheritanceDerivableTag<T>) tag).tryDeriveFromInheritance(inherited, tags)
                : none();
    }
}