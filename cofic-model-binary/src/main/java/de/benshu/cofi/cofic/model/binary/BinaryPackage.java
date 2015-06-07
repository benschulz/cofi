package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.cofic.model.binary.internal.TypeReference;
import de.benshu.cofi.cofic.model.binary.internal.UnboundTypeList;
import de.benshu.cofi.cofic.model.binary.internal.UnboundTypeParameterList;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;

import java.util.stream.Stream;

import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolve;
import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolveList;

public class BinaryPackage implements BinaryTypeDeclaration {
    private final ImmutableSet<BinaryAnnotation> annotations;
    private final Fqn fqn;
    private final String name;
    private final UnboundTypeParameterList typeParameters;
    private final UnboundTypeList supertypes;
    private final BinaryTypeBody body;
    private final ImmutableSet<AbstractBinaryTypeDeclaration> topLevelDeclarations;
    private final ImmutableSet<BinaryPackage> subpackages;

    public BinaryPackage(
            Ancestry ancestry,
            ImmutableSet<Constructor<BinaryAnnotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            ImmutableList<TypeReference> supertypes,
            Constructor<BinaryTypeBody> body,
            ImmutableSet<Constructor<AbstractBinaryTypeDeclaration>> topLevelDeclarations,
            ImmutableSet<Constructor<BinaryPackage>> subpackages) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.annotations = ancestryIncludingMe.constructAll(annotations);
        this.fqn = ancestry.closest(BinaryPackage.class).map(BinaryPackage::getFqn)
                .getOrSupply(() -> ancestry.closest(BinaryModule.class).get().getFqn().getParent())
                .getChild(name);
        this.name = name;
        this.typeParameters = resolve(ancestryIncludingMe, typeParameters);
        this.supertypes = resolveList(ancestryIncludingMe, supertypes);
        this.body = ancestryIncludingMe.construct(body);
        this.topLevelDeclarations = ancestryIncludingMe.constructAll(topLevelDeclarations);
        this.subpackages = ancestryIncludingMe.constructAll(subpackages);
    }

    public Fqn getFqn() {
        return fqn;
    }

    @Override
    public Stream<? extends BinaryMemberDeclaration> getMemberDeclarations() {
        throw null;
    }

    @Override
    public BinaryTypeBody getBody() {
        return body;
    }

    @Override
    public <X extends TypeSystemContext<X>> ProperTypeConstructorMixin<X, ?, ?> bind(X context) {
        throw null;
    }

    @Override
    public <X extends TypeSystemContext<X>> TypeParameterListImpl<X> getTypeParameters(X context) {
        return typeParameters.bind(context);
    }
}
