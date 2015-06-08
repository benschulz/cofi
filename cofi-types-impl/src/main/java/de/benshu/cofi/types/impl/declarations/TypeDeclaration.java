package de.benshu.cofi.types.impl.declarations;

import de.benshu.cofi.types.tags.IndividualTags;

public interface TypeDeclaration<X, B> {
    <O> O supplyTags(X context, B bound, Interpreter<IndividualTags, O> interpreter);
}
