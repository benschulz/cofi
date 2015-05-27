package de.benshu.cofi.cofic.frontend.companions;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.ObjectDeclaration;

import java.util.Map;

import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.commons.core.streams.Collectors.biMap;

public class CompanionDataBuilder {
    private final ImmutableList.Builder<Map.Entry<AbstractTypeDeclaration<Pass>, ObjectDeclaration<Pass>>> companions = ImmutableList.builder();

    public CompanionDataBuilder addAll(CompanionDataBuilder other) {
        companions.addAll(other.companions.build());

        return this;
    }

    public ObjectDeclaration<Pass> defineCompanion(AbstractTypeDeclaration<Pass> typeDeclaration, ObjectDeclaration<Pass> companion) {
        companions.add(immutableEntry(typeDeclaration, companion));
        return companion;
    }

    public CompanionData build() {
        return new CompanionData(companions.build().stream().collect(biMap()));
    }
}
