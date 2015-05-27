package de.benshu.cofi.model.impl;

import de.benshu.cofi.parser.AstNodeConstructorMethod;

public class InitializerStatement<X extends ModelContext<X>> extends TypeBody.Element<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> InitializerStatement<X> of(Statement<X> statement) {
        return new InitializerStatement<>(statement);
    }

    public final Statement<X> statement;

    public InitializerStatement(Statement<X> statement) {
        this.statement = statement;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitInitializerStatement(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> L accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformInitializerStatement(this);
    }

    @Override
    boolean isMember() {
        return false;
    }
}
