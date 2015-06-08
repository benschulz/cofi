package de.benshu.cofi.binary.internal;

import java.util.stream.Stream;

public interface BinaryTypeDeclarationMixin {
    Stream<? extends BinaryMemberDeclarationMixin> getMemberDeclarations();
}
