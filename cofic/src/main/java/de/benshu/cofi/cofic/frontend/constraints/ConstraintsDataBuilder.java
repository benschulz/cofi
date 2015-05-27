package de.benshu.cofi.cofic.frontend.constraints;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.GenericModelDataBuilder;
import de.benshu.cofi.model.impl.TypeParameters;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;

import java.util.Map;

import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.commons.core.streams.Collectors.map;

public class ConstraintsDataBuilder extends GenericModelDataBuilder<ConstraintsDataBuilder, ConstraintsData> {
    private final ImmutableList.Builder<Map.Entry<TypeParameters<Pass>, AbstractConstraints<Pass>>> typeParameterConstraints = ImmutableList.builder();

    @Override
    protected ConstraintsDataBuilder self() {
        return this;
    }

    public ConstraintsDataBuilder addAll(ConstraintsDataBuilder other) {
        typeParameterConstraints.addAll(other.typeParameterConstraints.build());

        return super.addAll(other);
    }


    public ConstraintsDataBuilder defineConstraintsOf(TypeParameters<Pass> typeParameters, AbstractConstraints<Pass> constraints) {
        typeParameterConstraints.add(immutableEntry(typeParameters, constraints));

        return this;
    }

    public ConstraintsData build() {
        return new ConstraintsData(
                buildTypeExpressionTypes(),
                typeParameterConstraints.build().stream().collect(map())
        );
    }
}
