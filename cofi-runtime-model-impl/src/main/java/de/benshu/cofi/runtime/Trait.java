package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.runtime.internal.Ancestry;
import de.benshu.cofi.runtime.internal.Constructor;
import de.benshu.cofi.runtime.internal.MemoizingSupplier;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.TypeList;
import de.benshu.commons.core.Debuggable;
import de.benshu.commons.core.Optional;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Trait extends AbstractTypeDeclaration<TemplateTypeConstructor> {
    final boolean trait = true;

    final Supplier<TypeList<TemplateTypeConstructor>> supertypes = MemoizingSupplier.of(() -> getType().getSupertypes());
    final Companion companion;

    public Trait(Ancestry ancestry,
                 ImmutableSet<Constructor<Annotation>> annotations,
                 String name,
                 TypeParameterListReference typeParameters,
                 Function<TypeDeclaration, TemplateTypeConstructor> type,
                 Constructor<TypeBody> body,
                 Constructor<Companion> companion) {
        super(ancestry, annotations, name, typeParameters, type, body);

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.companion = ancestryIncludingMe.construct(companion);
    }

    @Override
    public <R> R accept(NamedEntityVisitor<R> visitor) {
        return visitor.visitTrait(this);
    }

    @Override
    public Optional<Companion> getCompanion() {
        return Optional.some(companion);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String debug() {
        return "trait " + getName() + " extends " + supertypes.get().stream()
                .map(Debuggable::debug).collect(Collectors.joining(", "));
    }
}
