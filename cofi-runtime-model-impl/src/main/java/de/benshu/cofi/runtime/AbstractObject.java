package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.binary.internal.MemoizingSupplier;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.TypeList;
import de.benshu.commons.core.Debuggable;
import de.benshu.commons.core.Optional;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractObject extends AbstractTypeDeclaration<TemplateTypeConstructor> {
    final Supplier<TypeList<TemplateTypeConstructor>> supertypes = MemoizingSupplier.of(() -> getType().getSupertypes());

    public AbstractObject(
            Ancestry ancestry,
            ImmutableSet<Constructor<Annotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            Function<TypeDeclaration, TemplateTypeConstructor> type,
            Constructor<TypeBody> typeBody) {
        super(ancestry, annotations, name, typeParameters, type, typeBody);
    }

    @Override
    public Optional<Companion> getCompanion() {
        return Optional.none();
    }

    @Override
    public String debug() {
        return "object " + getName() + " extends " + supertypes.get().stream()
                .map(Debuggable::debug).collect(Collectors.joining(", "));
    }
}
