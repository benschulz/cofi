package de.benshu.cofi.types.impl.declarations;

import de.benshu.cofi.types.impl.TypeSystemContext;

public interface MemberDeclaration<X extends TypeSystemContext<X>> {
    <O> O supplyMembers(X context, Interpreter<SourceMemberDescriptors<X>, O> interpreter);
}
