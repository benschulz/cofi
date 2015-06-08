package de.benshu.cofi.types.impl.unions;

import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.UnboundProperTypeConstructor;
import de.benshu.cofi.types.impl.declarations.UnionTypeDeclaration;
import de.benshu.cofi.types.impl.tags.Tagger;

public class UnboundUnionTypeConstructor<X extends TypeSystemContext<X>> implements UnboundProperTypeConstructor<X> {
    private final UnionTypeDeclaration<X> declaration;

    UnboundUnionTypeConstructor(UnionTypeDeclaration<X> declaration) {
        this.declaration = declaration;
    }

    public UnionTypeDeclaration<X> getDeclaration() {
        return declaration;
    }

    public AbstractUnionTypeConstructor<X> bind(X context) {
        return new DerivedUnionTypeConstructor<>(this, context, Tagger.of(context, getDeclaration()));
    }
}
