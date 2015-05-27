package de.benshu.cofi.types.tags;

import de.benshu.commons.core.Optional;

import java.util.function.Function;

public class UnambiguouslyDerivableTag<T>
        implements Tag<T>,
                   ApplicationDerivableTag<T>,
                   DirectlyDerivableTag<T>,
                   InheritanceDerivableTag<T>,
                   SubstitutionDerivableTag<T> {

    private final String name;
    private final Function<Tags, Optional<T>> directDerivation;

    UnambiguouslyDerivableTag(String name, Function<Tags, Optional<T>> directDerivation) {
        this.name = name;
        this.directDerivation = directDerivation;
    }

    @Override
    public Optional<T> tryDeriveDirectly(Tags tags) {
        return Optional.from(directDerivation)
                .flatMap(d -> d.apply(tags));
    }

    @Override
    public Optional<T> tryDeriveFromApplication(Tags unapplied, Tags applied) {
        return unapplied.tryGet(this);
    }

    @Override
    public Optional<T> tryDeriveFromInheritance(Tags inherited, Tags all) {
        return inherited.tryGet(this);
    }

    @Override
    public Optional<T> tryDeriveFromSubstitution(Tags unsubstituted, Tags substituted) {
        return unsubstituted.tryGet(this);
    }

    @Override
    public String debug() {
        return name;
    }

    public static class Individual<T> extends UnambiguouslyDerivableTag<T> implements IndividualTag<T> {
        Individual(String name, Function<Tags, Optional<T>> directDerivation) {
            super(name, directDerivation);
        }
    }
}
