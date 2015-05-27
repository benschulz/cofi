package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.Namespace;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.members.AbstractMember;

public abstract class AbstractResolution implements Namespace.Resolution<Pass> {
    public abstract TypeMixin<Pass, ?> getType();

    public abstract boolean isMember();

    public abstract AbstractMember<Pass> getMember();

    public abstract boolean isError();

    public abstract ExpressionNode<Pass> getImplicitPrimary();
}
