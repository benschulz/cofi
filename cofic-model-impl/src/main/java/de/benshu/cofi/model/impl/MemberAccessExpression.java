package de.benshu.cofi.model.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.types.impl.members.AbstractMember;

import static java.util.stream.Collectors.joining;

public class MemberAccessExpression<X extends ModelContext<X>> extends ExpressionNode<X> implements MemberAccess<X> {
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
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> E accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformMemberAccessExpression(this);
    }

    public AbstractMember<X> getMember(X pass) {
        // TODO maybe cache?
        AbstractMember<X> member = pass.lookUpTypeOf(primary).lookupMember(name.ids.get(0).getLexeme()).get();
        Preconditions.checkState(member != null);
        return member;
    }

    @Override
    public ExpressionMixin<X> getPrimary() {
        return primary;
    }

    @Override
    public String toString() {
        String typeArgs = name.typeArgs == null ? "" : "<" + name.typeArgs.stream().map(Object::toString).collect(joining(", ")) + ">";
        return primary + "." + Iterables.getOnlyElement(name.ids) + typeArgs;
    }
}
