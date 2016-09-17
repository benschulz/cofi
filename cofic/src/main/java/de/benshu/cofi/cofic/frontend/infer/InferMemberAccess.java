package de.benshu.cofi.cofic.frontend.infer;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.ModelNode;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.commons.core.Optional;

public interface InferMemberAccess<T> {
    String getName();

    Optional<AbstractTypeList<Pass, ?>> getTypeArgs();

    T setTypeArgs(AbstractMember<Pass> member, AbstractTypeList<Pass, ?> typeArgs, T aggregate);

    ImmutableList<ModelNode<Pass>> getPath();
}