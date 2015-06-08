package de.benshu.cofi.types.impl.declarations;

import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptors;

public interface MemberDeclaration<X extends TypeSystemContext<X>> {
    <O> O supplyMembers(X context, Interpreter<SourceMemberDescriptors<X>, O> interpreter);
}
