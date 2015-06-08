package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.source.SourceType;

public interface HierarchyDeclaration<X extends TypeSystemContext<X>> {
    <O> O supplyHierarchy(X context, Interpreter<ImmutableList<SourceType<X>>, O> interpreter);
}
