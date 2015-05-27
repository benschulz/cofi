package de.benshu.cofi.types.tags;

import com.google.common.collect.ImmutableSet;
import de.benshu.commons.core.Optional;

public interface UnionDerivableTag<T> extends DerivableTag<T> {
    Optional<T> tryDeriveFromUnion(ImmutableSet<Tags> elements, Tags united);
}
