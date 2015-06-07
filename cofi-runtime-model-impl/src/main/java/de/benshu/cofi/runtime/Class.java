package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.binary.internal.MemoizingSupplier;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.TypeList;
import de.benshu.commons.core.Debuggable;
import de.benshu.commons.core.Optional;
import de.benshu.jswizzle.data.Data;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Class extends AbstractTypeDeclaration<TemplateTypeConstructor> implements Instantiatable, ClassAccessors {
    final Supplier<TypeList<TemplateTypeConstructor>> supertypes = MemoizingSupplier.of(() -> getType().getSupertypes());
    @Data
    final ImmutableList<Parameter> parameters;
    final Companion companion;

    public Class(
            Ancestry ancestry,
            ImmutableSet<Constructor<Annotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            Function<TypeDeclaration, TemplateTypeConstructor> type,
            ImmutableList<Constructor<Parameter>> parameters,
            Constructor<TypeBody> body,
            Constructor<Companion> companion) {
        super(ancestry, annotations, name, typeParameters, type, body);

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.parameters = ancestryIncludingMe.constructAll(parameters);
        this.companion = ancestryIncludingMe.construct(companion);
    }

    @Override
    public <R> R accept(NamedEntityVisitor<R> visitor) {
        return visitor.visitClass(this);
    }

    @Override
    public Optional<Companion> getCompanion() {
        return Optional.some(companion);
    }

    @Override
    public String debug() {
        return "class " + getName() + " extends " + supertypes.get().stream()
                .map(Debuggable::debug).collect(Collectors.joining(", "));
    }
}
