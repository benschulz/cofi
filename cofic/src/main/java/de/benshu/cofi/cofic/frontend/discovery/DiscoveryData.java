package de.benshu.cofi.cofic.frontend.discovery;

import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.TypeParameterized;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;

public class DiscoveryData {
    public static DiscoveryDataBuilder builder() {
        return new DiscoveryDataBuilder();
    }

    public final ImmutableMap<AbstractTypeDeclaration<Pass>, ProperTypeConstructorMixin<Pass, ?, ?>> types;
    public final ImmutableMap<TypeParameterized<Pass>, TypeParameterListImpl<Pass>> typeParameters;

    DiscoveryData(ImmutableMap<AbstractTypeDeclaration<Pass>, ProperTypeConstructorMixin<Pass, ?, ?>> types,
                  ImmutableMap<TypeParameterized<Pass>, TypeParameterListImpl<Pass>> typeParameters) {
        this.types = types;
        this.typeParameters = typeParameters;
    }
}
