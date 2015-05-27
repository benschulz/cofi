package de.benshu.cofi.types.tags;

import de.benshu.commons.core.Optional;

public interface DirectlyDerivableTag<T> extends DerivableTag<T> {
    Optional<T> tryDeriveDirectly(Tags tags);
}
