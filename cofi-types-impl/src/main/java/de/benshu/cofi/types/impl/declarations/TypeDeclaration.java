package de.benshu.cofi.types.impl.declarations;

import de.benshu.cofi.types.tags.IndividualTags;

public interface TypeDeclaration<X> {
    <O> O supplyTags(X context, Interpreter<IndividualTags, O> interpreter);
}
