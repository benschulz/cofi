package de.benshu.cofi.cofic.model.binary;

import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.commons.core.Optional;

import java.util.stream.Stream;

import static de.benshu.commons.core.Optional.none;

public interface BinaryTypeDeclaration extends BinaryMemberDeclaration {
    default Optional<BinaryCompanion> getCompanion() {
        return none();
    }

    Stream<? extends BinaryMemberDeclaration> getMemberDeclarations();

    BinaryTypeBody getBody();

    <X extends TypeSystemContext<X>> ProperTypeConstructorMixin<X, ?, ?> bind(X context);
}
