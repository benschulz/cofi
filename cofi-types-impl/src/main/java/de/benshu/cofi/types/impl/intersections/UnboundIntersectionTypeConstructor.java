package de.benshu.cofi.types.impl.intersections;

import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.UnboundProperTypeConstructor;
import de.benshu.cofi.types.impl.declarations.IntersectionTypeDeclaration;
import de.benshu.cofi.types.impl.tags.Tagger;

public class UnboundIntersectionTypeConstructor<X extends TypeSystemContext<X>> implements UnboundProperTypeConstructor<X> {
    private final IntersectionTypeDeclaration<X> declaration;

    UnboundIntersectionTypeConstructor(IntersectionTypeDeclaration<X> declaration) {
        this.declaration = declaration;
    }

    public IntersectionTypeDeclaration<X> getDeclaration() {
        return declaration;
    }

    public AbstractIntersectionTypeConstructor<X> bind(X context) {
        return new DerivedIntersectionTypeConstructor<>(this, context, Tagger.of(context, getDeclaration()));
    }
}
