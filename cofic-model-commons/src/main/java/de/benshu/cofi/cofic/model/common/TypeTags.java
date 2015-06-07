package de.benshu.cofi.cofic.model.common;

import de.benshu.cofi.types.impl.TypeName;
import de.benshu.cofi.types.tags.IndividualTag;
import de.benshu.cofi.types.tags.SimpleDefaultingIndividualTag;

public class TypeTags {
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
}
