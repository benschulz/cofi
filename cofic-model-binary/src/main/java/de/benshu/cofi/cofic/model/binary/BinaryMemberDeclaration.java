package de.benshu.cofi.cofic.model.binary;

import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;

public interface BinaryMemberDeclaration extends BinaryModelNode {
    <X extends TypeSystemContext<X>> TypeParameterListImpl<X> getTypeParameters(X context);
}
