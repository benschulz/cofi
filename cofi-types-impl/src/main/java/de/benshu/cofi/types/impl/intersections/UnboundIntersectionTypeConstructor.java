package de.benshu.cofi.types.impl.intersections;

import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.IntersectionTypeDeclaration;
import de.benshu.cofi.types.impl.tags.Tagger;

import static de.benshu.cofi.types.impl.declarations.Interpreter.id;

public class UnboundIntersectionTypeConstructor<X extends TypeSystemContext<X>> {
    private final IntersectionTypeDeclaration<X> declaration;

    UnboundIntersectionTypeConstructor(IntersectionTypeDeclaration<X> declaration) {
        this.declaration = declaration;
    }

    public IntersectionTypeDeclaration<X> getDeclaration() {
        return declaration;
    }

    public AbstractIntersectionTypeConstructor<X> bind(X context) {
        return new DerivedIntersectionTypeConstructor<>(this, context, Tagger.of(getDeclaration().supplyTags(context, id())));
    }
}
