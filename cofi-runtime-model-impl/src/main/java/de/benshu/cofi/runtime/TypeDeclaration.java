package de.benshu.cofi.runtime;

import de.benshu.cofi.binary.internal.BinaryTypeDeclarationMixin;
import de.benshu.cofi.types.ProperTypeConstructor;
import de.benshu.cofi.types.tags.IndividualTag;
import de.benshu.commons.core.Optional;

import java.util.stream.Stream;

import static de.benshu.commons.core.Optional.none;

public interface TypeDeclaration extends NamedEntity, MemberDeclaration, BinaryTypeDeclarationMixin {
    IndividualTag<TypeDeclaration> TAG = IndividualTag.named("TypeDeclaration").unambiguouslyDerivable();

    @Override
    default <R> R accept(MemberDeclarationVisitor<R> visitor) {
        return visitor.visitTypeDeclaration(this);
    }

    default Optional<Companion> getCompanion() {
        return none();
    }

    ProperTypeConstructor<?> getType();

    Stream<? extends MemberDeclaration> getMemberDeclarations();

    TypeBody getBody();
}
