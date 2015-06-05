package de.benshu.cofi.model.impl;

import com.google.common.base.Preconditions;
import de.benshu.cofi.parser.AstNodeConstructorMethod;

public class MemberAccessExpression<X extends ModelContext<X>> extends ExpressionNode<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> MemberAccessExpression<X> of(ExpressionNode<X> primary, RelativeNameImpl<X> name) {
        return new MemberAccessExpression<>(primary, name);
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> MemberAccessExpression<X> of(RelativeNameImpl<X> name) {
        return new MemberAccessExpression<>(null, name);
    }

    public final ExpressionNode<X> primary;
    public final RelativeNameImpl<X> name;

    public MemberAccessExpression(ExpressionNode<X> primary, RelativeNameImpl<X> name) {
        this.primary = primary == null ? null : primary;
        this.name = name;

        Preconditions.checkArgument(this.name.ids.size() == 1);
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitMemberAccessExpression(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> E accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformMemberAccessExpression(this);
    }
}
