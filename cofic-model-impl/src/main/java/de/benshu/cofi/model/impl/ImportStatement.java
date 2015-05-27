package de.benshu.cofi.model.impl;

import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.TokenString;

public class ImportStatement<X extends ModelContext<X>> extends AbstractModelNode<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> ImportStatement<X> of(FullyQualifiedName<X> name) {
        return of(name, null);
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> ImportStatement<X> of(FullyQualifiedName<X> name, TokenString wildcard) {
        return new ImportStatement<>(name, wildcard);
    }

    public final FullyQualifiedName<X> name;
    public final TokenString wildcard;

    public ImportStatement(FullyQualifiedName<X> name, TokenString wildcard) {
        this.name = name;
        this.wildcard = wildcard;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
    return     visitor.visitImportStatement(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformImportStatement(this);
    }
}
