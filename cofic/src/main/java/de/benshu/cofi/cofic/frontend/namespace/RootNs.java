package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;

public class RootNs extends GlueTypeNs {
    public static RootNs create() {
        return new RootNs();
    }

    public RootNs() {
        super();
    }

    @Override
    public AbstractConstraints<Pass> getContextualConstraints(LookUp lookUp) {
        return AbstractConstraints.none();
    }
}
