package de.benshu.cofi.types.impl.tags;

import de.benshu.cofi.types.impl.declarations.Interpreter;
import de.benshu.cofi.types.impl.declarations.TypeDeclaration;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.Tags;

public interface Tagger {
    static <X, T> Tagger of(X context, TypeDeclaration<X, T> declaration) {
        return t -> HashTags.create(t, declaration.supplyTags(context, (T) t, Interpreter.id()));
    }

    static Tagger of(IndividualTags individualTags) {
        return t -> HashTags.create(t, individualTags);
    }

    Tags tag(TaggedMixin tagged);
}
