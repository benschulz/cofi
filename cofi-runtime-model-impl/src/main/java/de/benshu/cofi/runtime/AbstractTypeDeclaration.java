package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.binary.internal.MemoizingSupplier;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.types.ProperTypeConstructor;
import de.benshu.cofi.types.TypeParameterList;
import de.benshu.jswizzle.data.Data;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static de.benshu.cofi.runtime.internal.Resolution.resolve;

@Data
public abstract class AbstractTypeDeclaration<T extends ProperTypeConstructor<?>>
        extends TypeBody.Containable
        implements TypeDeclaration,
                   MemberDeclaration,
                   TypeDeclarationAccessors<T> {

    final ImmutableSet<Annotation> annotations;
    final String name;
    @Data.Exclude
    final Supplier<TypeParameterList> typeParameters;
    @Data.Exclude
    final transient Supplier<T> type;
    final TypeBody body;

    public AbstractTypeDeclaration(
            Ancestry ancestry,
            ImmutableSet<Constructor<Annotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            Function<TypeDeclaration, T> type,
            Constructor<TypeBody> body) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.annotations = ancestryIncludingMe.constructAll(annotations);
        this.name = name;
        this.typeParameters = resolve(ancestryIncludingMe, typeParameters);
        this.type = MemoizingSupplier.of(() -> type.apply(this));
        this.body = ancestryIncludingMe.construct(body);
    }

    @Override
    public <R> R accept(MemberDeclarationVisitor<R> visitor) {
        return visitor.visitTypeDeclaration(this);
    }

    @Override
    public TypeBody getBody() {
        return body;
    }

    public final T getType() {
        return type.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TypeParameterList getTypeParameters() {
        return typeParameters.get();
    }

    @Override
    public Stream<? extends MemberDeclaration> getMemberDeclarations() {
        return body.getMemberDeclarations();
    }
}
