package de.benshu.cofi.cofic.model.binary;

import de.benshu.cofi.binary.deserialization.internal.BinaryMemberDeclarationMixin;
import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.declarations.source.CombinableSourceMemberDescriptor;

public interface BinaryMemberDeclaration extends BinaryModelNode, BinaryMemberDeclarationMixin {
    <X extends BinaryModelContext<X>> TypeParameterListImpl<X> bindTypeParameters(X context);

    Fqn getFqn();

    default String getName() {
        return getFqn().getLocalName();
    }

    <X extends BinaryModelContext<X>> CombinableSourceMemberDescriptor<X> toDescriptor(X context);
}
