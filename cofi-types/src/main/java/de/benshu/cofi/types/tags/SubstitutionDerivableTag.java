package de.benshu.cofi.types.tags;

import de.benshu.commons.core.Optional;

public interface SubstitutionDerivableTag<T> extends DerivableTag<T> {
    Optional<T> tryDeriveFromSubstitution(Tags unsubstituted, Tags substituted);
}
