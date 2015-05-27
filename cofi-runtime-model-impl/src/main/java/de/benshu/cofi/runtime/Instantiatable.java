package de.benshu.cofi.runtime;

import de.benshu.cofi.types.TemplateTypeConstructor;

public interface Instantiatable extends TypeDeclaration {
    @Override
    TemplateTypeConstructor getType();
}
