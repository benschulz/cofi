package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.GenericModelDataBuilder;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;

public class RootNs extends GlueTypeNs {
    public static RootNs create(Pass pass, GenericModelDataBuilder<?, ?> aggregate) {
        return new RootNs(pass, aggregate);
    }

    public RootNs(Pass pass, GenericModelDataBuilder<?, ?> aggregate) {
        super(pass, aggregate);
    }

    @Override
    public AbstractConstraints<Pass> getContextualConstraints() {
        return AbstractConstraints.none();
    }
}
