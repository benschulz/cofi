package de.benshu.cofi.types.impl.test;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.declarations.IntersectionTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.declarations.SourceType;
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.UnionTypeDeclaration;
import de.benshu.cofi.types.tags.IndividualTags;

public class TypeDeclarationFactory {
    public TemplateTypeDeclaration<TestContext> createTemplateTypeDeclaration(
            Supplier<TypeParameterListImpl<TestContext>> parametersSupplier,
            Supplier<ImmutableList<SourceType<TestContext>>> supertypesSupplier,
            Supplier<IndividualTags> tagsSupplier
    ) {
        return createTemplateTypeDeclaration(parametersSupplier::get, supertypesSupplier::get, SourceMemberDescriptors::empty, tagsSupplier);
    }

    public TemplateTypeDeclaration<TestContext> createTemplateTypeDeclaration(
            Supplier<TypeParameterListImpl<TestContext>> parametersSupplier,
            Supplier<ImmutableList<SourceType<TestContext>>> supertypesSupplier,
            Supplier<SourceMemberDescriptors<TestContext>> memberDescriptorsSupplier,
            Supplier<IndividualTags> tagsSupplier
    ) {
        return TemplateTypeDeclaration.memoizing(x -> parametersSupplier.get(), x -> supertypesSupplier.get(), x -> memberDescriptorsSupplier.get(), x -> tagsSupplier.get());
    }

    public IntersectionTypeDeclaration<TestContext> createIntersectionTypeDeclaration(
            Supplier<TypeParameterListImpl<TestContext>> parametersSupplier,
            Supplier<ImmutableList<SourceType<TestContext>>> elementsSupplier,
            Supplier<IndividualTags> tagsSupplier
    ) {
        return IntersectionTypeDeclaration.<TestContext>lazy(x -> parametersSupplier.get(), x -> elementsSupplier.get(), x -> tagsSupplier.get());
    }

    public UnionTypeDeclaration<TestContext> createUnionTypeDeclaration(
            Supplier<TypeParameterListImpl<TestContext>> parametersSupplier,
            Supplier<ImmutableList<SourceType<TestContext>>> elementsSupplier,
            Supplier<IndividualTags> tagsSupplier
    ) {
        return UnionTypeDeclaration.lazy(x -> parametersSupplier.get(), x -> elementsSupplier.get(), x -> tagsSupplier.get());
    }
}
