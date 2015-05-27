package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.model.TypeName;
import de.benshu.cofi.types.tags.SimpleDefaultingIndividualTag;
import de.benshu.cofi.types.tags.IndividualTag;
import de.benshu.cofi.types.tags.IndividualTags;

public class TypeTags {
    public static final SimpleDefaultingIndividualTag<Boolean> TRAIT = IndividualTag.named("IsTrait").defaultingTo(false);

    public static final SimpleDefaultingIndividualTag<TypeName> NAME = IndividualTag.named("Name").defaultingTo(() -> "<anonymous>");

    public static <X extends ModelContext<X>> IndividualTags getForTemplateType(AbstractTypeDeclaration<X> decl, ImmutableList<String> fqn, boolean trait) {
        return getForTemplateType(decl, FullyQualifiedTypeName.create(fqn), trait);
    }

    public static <X extends ModelContext<X>> IndividualTags getForTemplateType(AbstractTypeDeclaration<X> decl, TypeName name, boolean trait) {
        final IndividualTags tags = IndividualTags.empty()
                .set(NAME, name)
                .set(AbstractTypeDeclaration.Tag.INSTANCE, decl);

        return trait ? tags.set(TypeTags.TRAIT, true) : tags;
    }

    public static <X extends ModelContext<X>> IndividualTags getForTypeParameter(TypeParamDecl<X> declaration) {
        return IndividualTags.of(NAME, LocalTypeName.create(declaration.name.getLexeme()));
    }

    public static IndividualTags getForUnionType(ImmutableList<String> fqn) {
        return IndividualTags.of(NAME, FullyQualifiedTypeName.create(fqn));
    }

    public static <X extends ModelContext<X>> IndividualTags getForTopType(X context, ClassDeclaration<X> declaration) {
        return getForTemplateType(declaration, declaration::getName, false);
    }
}
