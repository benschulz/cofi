package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.source.SourceType;

public interface HierarchyDeclaration<X extends TypeSystemContext<X>, B> {
    <O> O supplyHierarchy(X context, B bound, Interpreter<ImmutableList<SourceType<X>>, O> interpreter);
}
