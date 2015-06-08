package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.binary.internal.MemoizingSupplier;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.types.ProperTypeConstructor;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.UnionTypeConstructor;
import de.benshu.commons.core.Debuggable;
import de.benshu.commons.core.Optional;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Union extends AbstractTypeDeclaration<UnionTypeConstructor> {
    final boolean union = true;

    final Supplier<TypeList<? extends ProperTypeConstructor<?>>> elements = MemoizingSupplier.of(() -> getType().getElements());
    private final Companion companion;

    public Union(
            Ancestry ancestry,
            ImmutableSet<Constructor<Annotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            Function<TypeDeclaration, UnionTypeConstructor> type,
            Constructor<TypeBody> body,
            Constructor<Companion> companion) {
        super(ancestry, annotations, name, typeParameters, type, body);

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.companion = ancestryIncludingMe.construct(companion);
    }

    @Override
    public <R> R accept(NamedEntityVisitor<R> visitor) {
        return visitor.visitUnion(this);
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
        return "union " + getName() + " extends " + elements.get().stream()
                .map(Debuggable::debug).collect(Collectors.joining(", "));
    }
}
