package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.types.impl.TypeMixin;

class EmptyNamespace extends AbstractNamespace {
    public static EmptyNamespace create(AbstractNamespace parent, TypeMixin<Pass, ?> type) {
        return new EmptyNamespace(parent, type);
    }

    private final TypeMixin<Pass, ?> type;

    EmptyNamespace(AbstractNamespace parent, TypeMixin<Pass, ?> type) {
        super(parent);

        this.type = type;
    }

    @Override
    protected TypeMixin<Pass, ?> asType(LookUp lookUp) {
        return type;
    }
}
