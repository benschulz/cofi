package de.benshu.cofi.types.impl.declarations;

import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;

public interface ParameterizedTypeDeclaration<X extends TypeSystemContext<X>, B> {
    <O> O supplyParameters(X context, B bound, Interpreter<TypeParameterListImpl<X>, O> interpreter);
}
