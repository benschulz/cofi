package de.benshu.cofi.types.tags;

import com.google.common.collect.ImmutableSet;
import de.benshu.commons.core.Optional;

public interface IntersectionDerivableTag<T> extends DerivableTag<T> {
    Optional<T> tryDeriveFromIntersection(ImmutableSet<Tags> elements, Tags intersected);
}

