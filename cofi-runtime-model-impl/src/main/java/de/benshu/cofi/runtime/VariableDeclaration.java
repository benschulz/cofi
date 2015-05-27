package de.benshu.cofi.runtime;

import de.benshu.cofi.types.ProperType;

public interface VariableDeclaration extends ModelNode {
    String getName();

    ProperType getValueType();
}
