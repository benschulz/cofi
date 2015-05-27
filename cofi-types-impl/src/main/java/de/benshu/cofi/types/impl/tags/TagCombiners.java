package de.benshu.cofi.types.impl.tags;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.Tagged;
import de.benshu.cofi.types.tags.Tags;

public class TagCombiners {
    public static Tagger apply(Tags unapplied) {
        return t -> HashTags.create(t, new ApplicationDeriver(unapplied));
    }

    public static Tagger bequest(Tags inheritance) {
        return t -> HashTags.create(t, new InheritanceDeriver(inheritance));
    }

    public static Tagger intersect(ImmutableSet<Tags> elements) {
        return t -> HashTags.create(t, new IntersectionDeriver(elements));
    }

    public static Tagger refine(Tags unrefined, IndividualTags refinement) {
        return t -> HashTags.create(t, new RefinementDeriver(unrefined, refinement), refinement);
    }

    public static <X> Tagger setAll(Tagged tagged, IndividualTags tags) {
        return t -> HashTags.create(t, tagged.getTags().getIndividualTags().setAll(tags));
    }

    public static Tagger substitute(Tags unsubstituted) {
        return t -> HashTags.create(t, new SubstitutionDeriver(unsubstituted));
    }

    public static Tagger unite(ImmutableSet<Tags> elements) {
        return t -> HashTags.create(t, new UnionDeriver(elements));
    }
}
