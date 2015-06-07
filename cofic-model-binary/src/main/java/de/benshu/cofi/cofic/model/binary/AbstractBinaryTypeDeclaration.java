package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.binary.deserialization.internal.UnboundTypeParameterList;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.impl.TypeParameterListImpl;

import java.util.stream.Stream;

import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolve;

public abstract class AbstractBinaryTypeDeclaration
        extends BinaryTypeBody.Containable
        implements BinaryTypeDeclaration,
                   BinaryMemberDeclaration {

    private final ImmutableSet<BinaryAnnotation> annotations;
    private final Fqn fqn;
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
        this.fqn = ancestry.closest(BinaryMemberDeclaration.class).get().getFqn().getChild(name);
        this.typeParameters = resolve(ancestryIncludingMe, typeParameters);
        this.body = ancestryIncludingMe.construct(body);
    }

    @Override
    public Fqn getFqn() {
        return fqn;
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
    public <X extends BinaryModelContext<X>> TypeParameterListImpl<X> bindTypeParameters(X context) {
        return typeParameters.bind(context);
    }
}
