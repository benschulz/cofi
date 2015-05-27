package de.benshu.cofi.runtime;

import de.benshu.cofi.types.TemplateTypeConstructor;

public interface Multiton extends Instantiatable, TypeDeclaration {
    TemplateTypeConstructor getType();
}
