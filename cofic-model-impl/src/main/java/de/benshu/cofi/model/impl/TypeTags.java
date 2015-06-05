package de.benshu.cofi.model.impl;

import de.benshu.cofi.types.impl.TypeName;
import de.benshu.cofi.types.tags.IndividualTag;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.SimpleDefaultingIndividualTag;

public class TypeTags {
    public static final SimpleDefaultingIndividualTag<Boolean> TRAIT = IndividualTag.named("IsTrait").defaultingTo(false);

    public static final SimpleDefaultingIndividualTag<TypeName> NAME = IndividualTag.named("Name").defaultingTo(new TypeName() {
        @Override
        public String toDescriptor() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String debug() {
            return "<anonymous>";
        }
    });

    public static <X extends ModelContext<X>> IndividualTags getForTypeParameter(TypeParamDecl<X> declaration) {
        return IndividualTags.of(NAME, LocalTypeName.create(declaration.name.getLexeme()));
    }
}
