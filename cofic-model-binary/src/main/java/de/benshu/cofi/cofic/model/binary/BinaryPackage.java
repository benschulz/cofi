package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.binary.deserialization.internal.UnboundType;
import de.benshu.cofi.binary.deserialization.internal.UnboundTypeParameterList;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.cofic.model.binary.internal.TypeReference;
import de.benshu.cofi.cofic.model.common.FullyQualifiedTypeName;
import de.benshu.cofi.cofic.model.common.TypeTags;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.source.CombinableSourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import de.benshu.cofi.types.tags.IndividualTags;

import java.util.stream.Stream;

import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolve;
import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolveAll;
import static de.benshu.commons.core.streams.Collectors.list;
import static de.benshu.commons.core.streams.Collectors.set;
import static de.benshu.commons.core.streams.Collectors.setMultimap;
import static java.util.function.Function.identity;

public class BinaryPackage implements BinaryTypeDeclaration {
    private final ImmutableSet<BinaryAnnotation> annotations;
    private final Fqn fqn;
    private final String name;
    private final UnboundTypeParameterList typeParameters;
    private final ImmutableList<UnboundType> supertypes;
    private final BinaryTypeBody body;
    private final ImmutableSet<AbstractBinaryTypeDeclaration> topLevelDeclarations;
    private final ImmutableSet<BinaryPackage> subpackages;

    public BinaryPackage(
            Ancestry ancestry,
            ImmutableSet<Constructor<BinaryAnnotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            ImmutableList<TypeReference> supertypes,
            Constructor<BinaryTypeBody> body,
            ImmutableSet<Constructor<AbstractBinaryTypeDeclaration>> topLevelDeclarations,
            ImmutableSet<Constructor<BinaryPackage>> subpackages) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.annotations = ancestryIncludingMe.constructAll(annotations);
        this.fqn = ancestry.closest(BinaryPackage.class).map(BinaryPackage::getFqn)
                .getOrSupply(() -> ancestry.closest(BinaryModule.class).get().getFqn().getParent())
                .getChild(name);
        this.name = name;
        this.typeParameters = resolve(ancestryIncludingMe, typeParameters);
        this.supertypes = resolveAll(ancestryIncludingMe, supertypes);
        this.body = ancestryIncludingMe.construct(body);
        this.topLevelDeclarations = ancestryIncludingMe.constructAll(topLevelDeclarations);
        this.subpackages = ancestryIncludingMe.constructAll(subpackages);
    }

    public Fqn getFqn() {
        return fqn;
    }

    @Override
    public Stream<? extends BinaryMemberDeclaration> getMemberDeclarations() {
        return Stream.of(
                subpackages.stream(),
                body.getMemberDeclarations(),
                topLevelDeclarations.stream()
                        .map(d -> d.getCompanion().<AbstractBinaryTypeDeclaration>map(c -> c).getOrReturn(d))
        ).flatMap(s -> s);
    }

    @Override
    public BinaryTypeBody getBody() {
        return body;
    }

    @Override
    public <X extends BinaryModelContext<X>> TemplateTypeConstructorMixin<X> bind(X context) {
        return AbstractTemplateTypeConstructor.<X>create(TemplateTypeDeclaration.memoizing(
                this::bindTypeParameters,
                x -> supertypes.stream().map(t -> SourceType.of(t.bind(x))).collect(list()),
                x -> SourceMemberDescriptors.create(typeParameters.bind(x).getConstraints(), getMemberDeclarations()
                        .map(d -> d.toDescriptor(context))
                        .collect(setMultimap(SourceMemberDescriptor::getName, identity()))
                        .asMap().values().stream()
                        .map(ds -> ds.stream().reduce(CombinableSourceMemberDescriptor::combineWith).get())
                        .collect(set())),
                x -> IndividualTags.of(TypeTags.NAME, FullyQualifiedTypeName.create(getFqn()))
        )).bind(context);
    }

    @Override
    public <X extends BinaryModelContext<X>> TypeParameterListImpl<X> bindTypeParameters(X context) {
        return typeParameters.bind(context);
    }
}
