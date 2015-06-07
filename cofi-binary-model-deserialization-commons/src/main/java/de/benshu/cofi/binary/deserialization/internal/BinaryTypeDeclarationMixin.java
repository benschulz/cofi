package de.benshu.cofi.binary.deserialization.internal;

import java.util.stream.Stream;

public interface BinaryTypeDeclarationMixin extends UnboundType {
    Stream<? extends BinaryMemberDeclarationMixin> getMemberDeclarations();
}
