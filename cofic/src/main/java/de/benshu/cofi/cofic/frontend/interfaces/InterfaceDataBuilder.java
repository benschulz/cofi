package de.benshu.cofi.cofic.frontend.interfaces;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.GenericModelData;
import de.benshu.cofi.cofic.frontend.GenericModelDataBuilder;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.MemberDeclarationImpl;
import de.benshu.cofi.types.impl.declarations.SourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.SourceMemberDescriptors;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Pair;

import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.commons.core.streams.Collectors.map;
import static de.benshu.commons.core.streams.Collectors.setMultimap;

public class InterfaceDataBuilder extends GenericModelDataBuilder<InterfaceDataBuilder, InterfaceData> {
    // TODO i want to get rid of this here..
    private final Pass pass;

    private final ImmutableList.Builder<Map.Entry<AbstractTypeDeclaration<Pass>, Fqn>> typeDeclarationFqns = ImmutableList.builder();
    private final ImmutableList.Builder<Map.Entry<MemberDeclarationImpl<Pass>, AbstractTypeDeclaration<Pass>>> containers = ImmutableList.builder();
    private final ImmutableList.Builder<Map.Entry<Pair<AbstractTypeDeclaration<Pass>, String>, SourceMethodSignatureDescriptorImpl>> methodSignatures = ImmutableList.builder();
    private final ImmutableList.Builder<Map.Entry<AbstractTypeDeclaration<Pass>, SourcePropertyDescriptorImpl>> properties = ImmutableList.builder();
    private final ImmutableList.Builder<Map.Entry<AbstractTypeDeclaration<Pass>, SourceTypeDescriptorImpl>> types = ImmutableList.builder();

    InterfaceDataBuilder(Pass pass, GenericModelData genericModelData) {
        super(genericModelData);

        this.pass = pass;
    }

    @Override
    protected InterfaceDataBuilder self() {
        return this;
    }

    public InterfaceDataBuilder addAll(InterfaceDataBuilder other) {
        typeDeclarationFqns.addAll(other.typeDeclarationFqns.build());
        containers.addAll(other.containers.build());
        methodSignatures.addAll(other.methodSignatures.build());
        properties.addAll(other.properties.build());
        types.addAll(other.types.build());

        return super.addAll(other);
    }

    public InterfaceDataBuilder defineFqnOf(AbstractTypeDeclaration<Pass> typeDeclaration, Fqn fqn) {
        typeDeclarationFqns.add(immutableEntry(typeDeclaration, fqn));

        return this;
    }

    public InterfaceDataBuilder defineContainer(MemberDeclarationImpl<Pass> memberDeclaration, AbstractTypeDeclaration<Pass> container) {
        containers.add(immutableEntry(memberDeclaration, container));

        return this;
    }

    public InterfaceDataBuilder addMethodSignature(AbstractTypeDeclaration<Pass> owner, String name, SourceMethodSignatureDescriptorImpl methodSignatureDescriptor) {
        methodSignatures.add(immutableEntry(Pair.of(owner, name), methodSignatureDescriptor));

        return this;
    }

    public InterfaceDataBuilder addProperty(AbstractTypeDeclaration<Pass> owner, SourcePropertyDescriptorImpl propertyDescriptor) {
        properties.add(immutableEntry(owner, propertyDescriptor));

        return this;
    }

    public InterfaceDataBuilder addType(AbstractTypeDeclaration<Pass> owner, SourceTypeDescriptorImpl typeDescriptor) {
        types.add(immutableEntry(owner, typeDescriptor));

        return this;
    }

    public InterfaceData build() {
        return new InterfaceData(
                buildTypeExpressionTypes(),
                typeDeclarationFqns.build().stream().collect(map()),
                containers.build().stream().collect(map()),
                buildMemberDescriptors()
        );
    }

    private ImmutableMap<AbstractTypeDeclaration<Pass>, SourceMemberDescriptors<Pass>> buildMemberDescriptors() {
        return Stream.concat(Stream.concat(buildMethodDescriptors(), properties.build().stream()), types.build().stream())
                .collect(setMultimap()).asMap().entrySet().stream()
                .map(e -> toMemberDescriptors(e.getKey(), ImmutableSet.copyOf(e.getValue())))
                .collect(map());
    }

    private Map.Entry<AbstractTypeDeclaration<Pass>, SourceMemberDescriptors<Pass>> toMemberDescriptors(
            AbstractTypeDeclaration<Pass> owner,
            ImmutableSet<SourceMemberDescriptor<Pass>> memberDescriptors) {

        return immutableEntry(owner, SourceMemberDescriptors.create(pass.lookUpTypeParametersOf(owner).getConstraints(), memberDescriptors));
    }

    private Stream<Map.Entry<AbstractTypeDeclaration<Pass>, SourceMethodDescriptorImpl>> buildMethodDescriptors() {
        return methodSignatures.build().stream()
                .collect(setMultimap()).asMap().entrySet().stream()
                .map(e -> toMethodDescriptor(e.getKey().a, e.getKey().b, ImmutableSet.copyOf(e.getValue())));
    }

    private Map.Entry<AbstractTypeDeclaration<Pass>, SourceMethodDescriptorImpl> toMethodDescriptor(
            AbstractTypeDeclaration<Pass> owner,
            String name,
            ImmutableSet<SourceMethodSignatureDescriptorImpl> signatureDescriptors) {

        final IndividualTags tags = IndividualTags.empty();

        final SourceMethodDescriptorImpl descriptor = new SourceMethodDescriptorImpl(name, signatureDescriptors, tags);

        return immutableEntry(owner, descriptor);
    }
}
