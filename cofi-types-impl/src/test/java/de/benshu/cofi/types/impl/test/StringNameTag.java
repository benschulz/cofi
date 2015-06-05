package de.benshu.cofi.types.impl.test;

import de.benshu.cofi.types.impl.TypeName;
import de.benshu.cofi.types.tags.DefaultingTag;
import de.benshu.cofi.types.tags.HashIndividualTags;
import de.benshu.cofi.types.tags.IndividualTag;
import de.benshu.cofi.types.tags.IndividualTags;

public enum StringNameTag implements IndividualTag<TypeName>, DefaultingTag<TypeName> {
    INSTANCE;

    private static class StringTypeName implements TypeName {
        private final String string;

        private StringTypeName(String string) {
            this.string = string;
        }

        @Override
        public String debug() {
            return string;
        }

        @Override
        public String toDescriptor() {
            return string;
        }
    }

    @Override
    public String debug() {
        return "Name";
    }

    @Override
    public TypeName getDefault() {
        return new StringTypeName("<anonymous>");
    }

    public static IndividualTags labeled(String name) {
        return HashIndividualTags.of(INSTANCE, new StringTypeName(name));
    }
}