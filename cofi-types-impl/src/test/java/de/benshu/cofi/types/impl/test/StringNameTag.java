package de.benshu.cofi.types.impl.test;

import de.benshu.cofi.types.tags.DefaultingTag;
import de.benshu.cofi.types.tags.HashIndividualTags;
import de.benshu.cofi.types.tags.IndividualTag;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Debuggable;

public enum StringNameTag implements IndividualTag<Debuggable>, DefaultingTag<Debuggable> {
    INSTANCE;

    private static class DebuggableString implements Debuggable {
        private final String string;

        private DebuggableString(String string) {
            this.string = string;
        }

        @Override
        public String debug() {
            return string;
        }
    }

    @Override
    public String debug() {
        return "Name";
    }

    @Override
    public Debuggable getDefault() {
        return new DebuggableString("<anonymous>");
    }

    public static IndividualTags labeled(String name) {
        return HashIndividualTags.of(INSTANCE, new DebuggableString(name));
    }
}