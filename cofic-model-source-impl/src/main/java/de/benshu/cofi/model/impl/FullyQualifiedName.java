package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.commons.core.streams.Collectors;

public class FullyQualifiedName<X extends ModelContext<X>> extends NameImpl<X> {
    public static <X extends ModelContext<X>> FullyQualifiedName<X> create(Token... ids) {
        return create(ImmutableList.copyOf(ids));
    }

    public static <X extends ModelContext<X>> FullyQualifiedName<X> create(ImmutableList<Token> ids) {
        return of(ids, ImmutableList.<TypeExpression<X>>of());
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> FullyQualifiedName<X> of(ImmutableList<Token> ids, ImmutableList<TypeExpression<X>> typeArgs) {
        return new FullyQualifiedName<>(ids, typeArgs);
    }

    public final Fqn fqn;

    private FullyQualifiedName(ImmutableList<Token> ids, ImmutableList<TypeExpression<X>> typeArgs) {
        super(ids, typeArgs);

        fqn = Fqn.from(ids.stream().map(Token::getLexeme).collect(Collectors.list()));
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitFullyQualifiedName(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformFullyQualifiedName(this);
    }
}
