package de.benshu.cofi.cofic.model.binary;

import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.binary.internal.BinaryTypeDeclarationMixin;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.declarations.source.CombinableSourceMemberDescriptor;
import de.benshu.commons.core.Optional;

import java.util.stream.Stream;

import static de.benshu.commons.core.Optional.none;

public interface BinaryTypeDeclaration extends BinaryMemberDeclaration, BinaryTypeDeclarationMixin {
    default Optional<BinaryCompanion> getCompanion() {
        return none();
    }

    Stream<? extends BinaryMemberDeclaration> getMemberDeclarations();

    BinaryTypeBody getBody();

    <X extends BinaryModelContext<X>> ProperTypeConstructorMixin<X, ?, ?> bind(X context);

    @Override
    default <X extends BinaryModelContext<X>> CombinableSourceMemberDescriptor<X> toDescriptor(X context) {
        return new TypeDeclarationDescriptor<>(context, this);
    }
}
