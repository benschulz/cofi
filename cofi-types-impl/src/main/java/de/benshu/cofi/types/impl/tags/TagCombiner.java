package de.benshu.cofi.types.impl.tags;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.types.tags.Tags;

public interface TagCombiner {
    default Tagger combine(Tags... tags) {
        return combine(ImmutableSet.copyOf(tags));
    }

    Tagger combine(ImmutableSet<Tags> tags);
}
