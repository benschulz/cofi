package de.benshu.cofi.types.impl.templates;

import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.UnboundProperTypeConstructor;
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.impl.tags.Tagger;

public class UnboundTemplateTypeConstructor<X extends TypeSystemContext<X>> implements UnboundProperTypeConstructor<X> {
    private final TemplateTypeDeclaration<X> declaration;

    public UnboundTemplateTypeConstructor(TemplateTypeDeclaration<X> declaration) {
        super();
        this.declaration = declaration;
    }

    public TemplateTypeDeclaration<X> getDeclaration() {
        return declaration;
    }

    public AbstractTemplateTypeConstructor<X> bind(X context) {
        return new DerivedTemplateTypeConstructor<>(this, context, Tagger.of(context, getDeclaration()));
    }
}
