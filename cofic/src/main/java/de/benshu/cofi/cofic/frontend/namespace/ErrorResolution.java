package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.types.impl.ErrorConstructor;
import de.benshu.cofi.types.impl.members.AbstractMember;

class ErrorResolution extends AbstractResolution {
    public ErrorResolution() { }

    @Override
    public ErrorConstructor<Pass> getType() {
        return ErrorConstructor.create();
    }

    @Override
    public boolean isMember() {
        return false;
    }

    @Override
    public AbstractMember<Pass> getMember() {
        throw null;
    }

    @Override
    public boolean isError() {
        return true;
    }

    @Override
    public ExpressionNode<Pass> getImplicitPrimary() {
        throw new IllegalStateException();
    }
}
