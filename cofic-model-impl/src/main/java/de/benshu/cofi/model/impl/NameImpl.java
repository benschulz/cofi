package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;

import de.benshu.cofi.model.Name;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.impl.AbstractTypeConstructor;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.members.AbstractMember;

public abstract class NameImpl<X extends ModelContext<X>> extends AbstractModelNode<X> implements Name<X> {
    public final ImmutableList<Token> ids;
    public final ImmutableList<TypeExpression<X>> typeArgs;

    public NameImpl(ImmutableList<Token> ids, ImmutableList<TypeExpression<X>> typeArgs) {
        this.ids = ids;
        this.typeArgs = typeArgs;
    }

    @Override
    public ReferentImpl<X> getReferent() {
        throw null;
    }

    public static final class ReferentImpl<X extends ModelContext<X>> implements Referent<X> {
        private ReferentImpl() { }

        @Override
        public ModelNodeMixin<X> getNode() {
            throw null;
        }

        @Override
        public AbstractTypeConstructor<X, ?, ? extends ProperTypeMixin<X, ?>> getType(X pass) {
            throw null;
        }

        @Override
        public boolean isMember() {
            throw null;
        }

        @Override
        public AbstractMember<X> getMember() {
            throw null;
        }

        public boolean isError() {
            throw null;
        }
    }
}
