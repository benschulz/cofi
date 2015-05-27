package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.runtime.internal.Ancestry;
import de.benshu.cofi.runtime.internal.Constructor;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.types.TemplateTypeConstructor;

import java.util.function.Function;

public class SingletonCompanion extends Companion implements Singleton {
    final boolean singleton = true;

    public SingletonCompanion(
            Ancestry ancestry,
            ImmutableSet<Constructor<Annotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            Function<TypeDeclaration, TemplateTypeConstructor> type,
            Constructor<TypeBody> body) {
        super(ancestry, annotations, name, typeParameters, type, body);
    }
}
