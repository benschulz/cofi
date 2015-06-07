package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.cofic.model.binary.internal.UnboundTypeParameterList;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;

import java.util.stream.Stream;

import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolve;

public abstract class AbstractBinaryTypeDeclaration
        extends BinaryTypeBody.Containable
        implements BinaryTypeDeclaration,
                   BinaryMemberDeclaration {

    private final ImmutableSet<BinaryAnnotation> annotations;
    private final String name;
    private final UnboundTypeParameterList typeParameters;
    private final BinaryTypeBody body;

    AbstractBinaryTypeDeclaration(
            Ancestry ancestry,
            ImmutableSet<Constructor<BinaryAnnotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            Constructor<BinaryTypeBody> body) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.annotations = ancestryIncludingMe.constructAll(annotations);
        this.name = name;
        this.typeParameters = resolve(ancestryIncludingMe, typeParameters);
        this.body = ancestryIncludingMe.construct(body);
    }

    @Override
    public Stream<? extends BinaryMemberDeclaration> getMemberDeclarations() {
        return body.getMemberDeclarations();
    }

    @Override
    public BinaryTypeBody getBody() {
        return body;
    }

    @Override
    public <X extends TypeSystemContext<X>> TypeParameterListImpl<X> getTypeParameters(X context) {
        return typeParameters.bind(context);
    }
}
