package de.benshu.cofi.cofic.frontend.discovery;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.TypeParameterized;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;

import java.util.Map;

import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.commons.core.streams.Collectors.map;

public class DiscoveryDataBuilder {
    private final ImmutableList.Builder<Map.Entry<AbstractTypeDeclaration<Pass>, ProperTypeConstructorMixin<Pass, ?, ?>>> types = ImmutableList.builder();
    private final ImmutableList.Builder<Map.Entry<TypeParameterized<Pass>, TypeParameterListImpl<Pass>>> typeParameters = ImmutableList.builder();

    DiscoveryDataBuilder() {}

    public DiscoveryDataBuilder addAll(DiscoveryDataBuilder other) {
        types.addAll(other.types.build());
        typeParameters.addAll(other.typeParameters.build());

        return this;
    }

    public DiscoveryDataBuilder defineTypeOf(AbstractTypeDeclaration<Pass> declaration, ProperTypeConstructorMixin<Pass, ?, ?> type) {
        types.add(immutableEntry(declaration, type));

        return this;
    }

    public DiscoveryDataBuilder defineTypeParametersOf(TypeParameterized<Pass> parameterized, TypeParameterListImpl<Pass> parameters) {
        typeParameters.add(immutableEntry(parameterized, parameters));

        return this;
    }

    public DiscoveryData build() {
        return new DiscoveryData(
                types.build().stream().collect(map()),
                typeParameters.build().stream().collect(map())
        );
    }
}
