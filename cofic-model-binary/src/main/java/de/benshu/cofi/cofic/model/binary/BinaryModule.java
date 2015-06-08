package de.benshu.cofi.cofic.model.binary;

import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.binary.internal.BinaryModuleMixin;
import de.benshu.cofi.binary.deserialization.internal.UnboundTypeParameterList;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;

import java.util.stream.Stream;

import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolve;

public class BinaryModule implements BinaryTypeDeclaration, BinaryModuleMixin {
    private final Fqn fqn;
    private final UnboundTypeParameterList typeParameters;
    private final BinaryPackage pakkage;

    public BinaryModule(
            Fqn fqn,
            TypeParameterListReference typeParameters,
            Constructor<BinaryPackage> pakkage) {

        final Ancestry ancestryIncludingMe = Ancestry.first(this);

        this.fqn = fqn;
        this.typeParameters = resolve(ancestryIncludingMe, typeParameters);
        this.pakkage = ancestryIncludingMe.construct(pakkage);
    }

    public Fqn getFqn() {
        return fqn;
    }

    @Override
    public Stream<? extends BinaryMemberDeclaration> getMemberDeclarations() {
        return pakkage.getMemberDeclarations();
    }

    @Override
    public BinaryTypeBody getBody() {
        throw null;
    }

    @Deprecated
    public BinaryPackage getPakkage() {
        return pakkage;
    }

    @Override
    public <X extends BinaryModelContext<X>> TemplateTypeConstructorMixin<X> bind(X context) {
        return pakkage.bind(context);
    }

    @Override
    public <X extends BinaryModelContext<X>> TypeParameterListImpl<X> bindTypeParameters(X context) {
        return typeParameters.bind(context);
    }
}
