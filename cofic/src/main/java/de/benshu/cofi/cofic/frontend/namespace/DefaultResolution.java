package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.members.AbstractMember;
import static com.google.common.base.Preconditions.checkArgument;

final class DefaultResolution extends AbstractResolution {
    private final TypeMixin<Pass, ?> type;
    private final ExpressionNode<Pass> implicitPrimary;
    private final AbstractMember<Pass> member;

    public DefaultResolution(TypeMixin<Pass, ?> type, ExpressionNode<Pass> implicitPrimary, AbstractMember<Pass> member) {
        checkArgument(member == null || implicitPrimary != null);

        this.type = type;
        this.implicitPrimary = implicitPrimary;
        this.member = member;
    }

    public DefaultResolution(TypeMixin<Pass, ?> type) {
        this(type, null, null);
    }

    @Override
    public TypeMixin<Pass, ?> getType() {
        return type;
    }

    @Override
    public boolean isMember() {
        return member != null;
    }

    @Override
    public AbstractMember<Pass> getMember() {
        if (!isMember()) {
            throw new UnsupportedOperationException();
        }

        return this.member;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public ExpressionNode<Pass> getImplicitPrimary() {
        if (!isMember())
            throw new IllegalStateException();

        return implicitPrimary;
    }
}
