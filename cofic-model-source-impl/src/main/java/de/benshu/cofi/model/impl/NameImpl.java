package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.model.Name;
import de.benshu.cofi.parser.lexer.Token;

public abstract class NameImpl<X extends ModelContext<X>> extends AbstractModelNode<X> implements Name<X> {
    public final ImmutableList<Token> ids;
    public final ImmutableList<TypeExpression<X>> typeArgs;

    public NameImpl(ImmutableList<Token> ids, ImmutableList<TypeExpression<X>> typeArgs) {
        this.ids = ids;
        this.typeArgs = typeArgs;
    }
}
