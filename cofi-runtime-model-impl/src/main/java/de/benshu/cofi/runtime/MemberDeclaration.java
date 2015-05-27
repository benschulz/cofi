package de.benshu.cofi.runtime;

import de.benshu.cofi.types.TypeParameterList;
import de.benshu.cofi.types.tags.IndividualTag;

public interface MemberDeclaration extends ModelNode {
    IndividualTag<MemberDeclaration> TAG = IndividualTag.named("MemberDeclaration").unambiguouslyDerivable();

    <R> R accept(MemberDeclarationVisitor<R> visitor);

    String getName();

    TypeParameterList getTypeParameters();
}
