package de.benshu.cofi.cofic.frontend.companions;

import com.google.common.collect.ImmutableBiMap;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.ObjectDeclaration;

public class CompanionData {
    public static CompanionDataBuilder builder() {
        return new CompanionDataBuilder();
    }

    public final ImmutableBiMap<AbstractTypeDeclaration<Pass>, ObjectDeclaration<Pass>> companions;

    CompanionData(ImmutableBiMap<AbstractTypeDeclaration<Pass>, ObjectDeclaration<Pass>> companions) {
        this.companions = companions;
    }
}
