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
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.source.CombinableSourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Optional;

import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolveAll;
import static de.benshu.commons.core.Optional.some;
import static de.benshu.commons.core.streams.Collectors.list;
import static de.benshu.commons.core.streams.Collectors.set;
import static de.benshu.commons.core.streams.Collectors.setMultimap;
import static java.util.function.Function.identity;

public class BinaryTrait extends AbstractBinaryTypeDeclaration {
    private final ImmutableList<UnboundType> supertypes;
    private final BinaryCompanion companion;

    BinaryTrait(
            Ancestry ancestry,
            ImmutableSet<Constructor<BinaryAnnotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            ImmutableList<TypeReference> supertypes,
            Constructor<BinaryTypeBody> body,
            Constructor<BinaryCompanion> companion) {
        super(ancestry, annotations, name, typeParameters, body);

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.supertypes = resolveAll(ancestryIncludingMe, supertypes);
        this.companion = ancestryIncludingMe.construct(companion);
    }

    @Override
    public Optional<BinaryCompanion> getCompanion() {
        return some(companion);
    }

    @Override
    public <X extends BinaryModelContext<X>> ProperTypeConstructorMixin<X, ?, ?> bind(X context) {
        return AbstractTemplateTypeConstructor.<X>create(TemplateTypeDeclaration.memoizing(
                this::bindTypeParameters,
                x -> supertypes.stream().map(t -> SourceType.of(t.bind(x))).collect(list()),
                x -> SourceMemberDescriptors.create(this.bindTypeParameters(x).getConstraints(), getMemberDeclarations()
                        .map(d -> d.toDescriptor(context))
                        .collect(setMultimap(SourceMemberDescriptor::getName, identity()))
                        .asMap().values().stream()
                        .map(ds -> ds.stream().reduce(CombinableSourceMemberDescriptor::combineWith).get())
                        .collect(set())),
                x -> IndividualTags.of(TypeTags.NAME, FullyQualifiedTypeName.create(getFqn()))
        )).bind(context);
    }
}
