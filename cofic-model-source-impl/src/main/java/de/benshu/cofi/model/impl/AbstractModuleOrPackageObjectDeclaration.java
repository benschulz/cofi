package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.lexer.Token;

public abstract class AbstractModuleOrPackageObjectDeclaration<X extends ModelContext<X>> extends AbstractObjectDeclaration<X> {
    AbstractModuleOrPackageObjectDeclaration(
            ImmutableList<AnnotationImpl<X>> annotations,
            ImmutableList<ModifierImpl<X>> modifiers,
            ImmutableList<TypeExpression<X>> extending,
            TypeBody<X> body) {
        super(annotations, modifiers, extending, body);
    }

    @Override
    public final Token getId() {
        throw new AssertionError();
    }
}
