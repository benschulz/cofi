package de.benshu.cofi.cofic.model.binary;

import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.types.impl.declarations.source.CombinableSourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.declarations.source.SourceTypeDescriptor;

class TypeDeclarationDescriptor<X extends BinaryModelContext<X>> implements SourceTypeDescriptor<X>, CombinableSourceMemberDescriptor<X> {
    private final X context;
    private final BinaryTypeDeclaration typeDeclaration;

    public TypeDeclarationDescriptor(X context, BinaryTypeDeclaration typeDeclaration) {
        this.context = context;
        this.typeDeclaration = typeDeclaration;
    }

    @Override
    public SourceType<X> getType() {
        return SourceType.<X>of(typeDeclaration.<X>bind(context));
    }

    @Override
    public String getName() {
        return typeDeclaration.getName();
    }

    @Override
    public CombinableSourceMemberDescriptor<X> combineWith(CombinableSourceMemberDescriptor<X> other) {
        throw new UnsupportedOperationException();
    }
}
