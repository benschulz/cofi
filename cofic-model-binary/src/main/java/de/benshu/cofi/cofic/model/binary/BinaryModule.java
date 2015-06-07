package de.benshu.cofi.cofic.model.binary;

import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;

import java.util.stream.Stream;

public class BinaryModule implements BinaryTypeDeclaration {
    private final Fqn fqn;
    private final BinaryPackage pakkage;

    public BinaryModule(
            Fqn fqn,
            Constructor<BinaryPackage> pakkage) {

        final Ancestry ancestryIncludingMe = Ancestry.first(this);

        this.fqn = fqn;
        this.pakkage = ancestryIncludingMe.construct(pakkage);
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
        throw null;
    }

    @Override
    public <X extends TypeSystemContext<X>> ProperTypeConstructorMixin<X, ?, ?> bind(X context) {
        throw null;
    }

    @Override
    public <X extends TypeSystemContext<X>> TypeParameterListImpl<X> getTypeParameters(X context) {
        return pakkage.getTypeParameters(context);
    }
}
