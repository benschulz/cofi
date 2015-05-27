package de.benshu.cofi.types.tags;

import de.benshu.commons.core.Optional;

public interface ApplicationDerivableTag<T> extends DerivableTag<T> {
    Optional<T> tryDeriveFromApplication(Tags unapplied, Tags applied);
}
