package de.benshu.cofi.cofic.model.binary;

import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;

public class BinaryPropertyDeclaration extends BinaryTypeBody.Containable implements BinaryMemberDeclaration {
    @Override
    public <X extends TypeSystemContext<X>> TypeParameterListImpl<X> getTypeParameters(X context) {
        throw null;
    }
}
