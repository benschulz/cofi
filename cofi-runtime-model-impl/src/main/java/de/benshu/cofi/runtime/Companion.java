package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.runtime.internal.Ancestry;
import de.benshu.cofi.runtime.internal.Constructor;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.types.TemplateTypeConstructor;

import java.util.function.Function;

public abstract class Companion extends AbstractObject implements Multiton {
    final boolean companion = true;

    final transient TypeDeclaration accompanied;

    public Companion(
            Ancestry ancestry,
            ImmutableSet<Constructor<Annotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            Function<TypeDeclaration, TemplateTypeConstructor> type,
            Constructor<TypeBody> body) {
        super(ancestry, annotations, name, typeParameters, type, body);

        this.accompanied = ancestry.closest(TypeDeclaration.class).get();
    }

    @Override
    public <R> R accept(NamedEntityVisitor<R> visitor) {
        return visitor.visitCompanion(this);
    }

    public TypeDeclaration getAccompanied() {
        return accompanied;
    }

    public static class MultitonCompanion extends Companion {
        public MultitonCompanion(
                Ancestry ancestry,
                ImmutableSet<Constructor<Annotation>> annotations,
                String name,
                TypeParameterListReference typeParameters,
                Function<TypeDeclaration, TemplateTypeConstructor> type,
                Constructor<TypeBody> body) {
            super(ancestry, annotations, name, x -> ancestry.closest(TypeDeclaration.class).get().getTypeParameters(), type, body);
        }
    }
}
