package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.binary.deserialization.internal.UnboundType;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.cofic.model.binary.internal.TypeReference;
import de.benshu.cofi.cofic.model.common.FullyQualifiedTypeName;
import de.benshu.cofi.cofic.model.common.TypeTags;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.declarations.UnionTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.unions.AbstractUnionTypeConstructor;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Optional;

import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolveAll;
import static de.benshu.commons.core.Optional.some;
import static de.benshu.commons.core.streams.Collectors.list;

public class BinaryUnion extends AbstractBinaryTypeDeclaration {
    private final ImmutableList<UnboundType> elements;
    private final BinaryCompanion companion;

    BinaryUnion(
            Ancestry ancestry,
            ImmutableSet<Constructor<BinaryAnnotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            ImmutableList<TypeReference> elements,
            Constructor<BinaryTypeBody> body,
            Constructor<BinaryCompanion> companion) {
        super(ancestry, annotations, name, typeParameters, body);

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.elements = resolveAll(ancestryIncludingMe, elements);
        this.companion = ancestryIncludingMe.construct(companion);
    }

    @Override
    public Optional<BinaryCompanion> getCompanion() {
        return some(companion);
    }

    @Override
    public <X extends BinaryModelContext<X>> ProperTypeConstructorMixin<X, ?, ?> bind(X context) {
        return AbstractUnionTypeConstructor.<X>create(UnionTypeDeclaration.lazy(
                this::bindTypeParameters,
                x -> elements.stream().map(t -> SourceType.of(t.bind(x))).collect(list()),
                x -> IndividualTags.of(TypeTags.NAME, FullyQualifiedTypeName.create(getFqn()))
        )).bind(context);
    }
}
