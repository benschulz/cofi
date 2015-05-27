package de.benshu.cofi.types.impl.declarations;

import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;

public interface InterpretedTypeDescriptor<X extends TypeSystemContext<X>> extends InterpretedMemberDescriptor<X> {
    ProperTypeConstructorMixin<X, ?, ?> getType(X context);
}
