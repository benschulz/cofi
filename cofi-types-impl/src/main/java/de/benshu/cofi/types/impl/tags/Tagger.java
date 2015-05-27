package de.benshu.cofi.types.impl.tags;

import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.Tags;

public interface Tagger {
    static Tagger of(IndividualTags individualTags) {
        return t -> HashTags.create(t, individualTags);
    }

    Tags tag(TaggedMixin tagged);
}
