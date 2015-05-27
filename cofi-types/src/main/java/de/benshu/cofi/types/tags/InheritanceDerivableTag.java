package de.benshu.cofi.types.tags;

import de.benshu.commons.core.Optional;

public interface InheritanceDerivableTag<T> extends DerivableTag<T> {
    Optional<T> tryDeriveFromInheritance(Tags inherited, Tags all);
}
