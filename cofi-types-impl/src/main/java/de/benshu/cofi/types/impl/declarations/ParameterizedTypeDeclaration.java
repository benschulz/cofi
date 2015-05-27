package de.benshu.cofi.types.impl.declarations;

import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeParameterListImpl;

public interface ParameterizedTypeDeclaration<X extends TypeSystemContext<X>> {
    <O> O supplyParameters(X context, Interpreter<TypeParameterListImpl<X>, O> interpreter);
}
