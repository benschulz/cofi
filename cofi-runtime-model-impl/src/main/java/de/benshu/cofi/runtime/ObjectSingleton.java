package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.types.TemplateTypeConstructor;

import java.util.function.Function;

public class ObjectSingleton extends AbstractObject implements Singleton {
    public ObjectSingleton(Ancestry ancestry,
                           ImmutableSet<Constructor<Annotation>> annotations,
                           String name,
                           TypeParameterListReference typeParameters,
                           Function<TypeDeclaration, TemplateTypeConstructor> type,
                           Constructor<TypeBody> body) {
        super(ancestry, annotations, name, typeParameters, type, body);
    }

    @Override
    public <R> R accept(NamedEntityVisitor<R> visitor) {
        return visitor.visitSingleton(this);
    }
}
