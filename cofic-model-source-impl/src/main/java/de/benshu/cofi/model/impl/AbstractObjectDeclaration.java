package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;

public abstract class AbstractObjectDeclaration<X extends ModelContext<X>> extends AbstractTypeDeclaration<X> {
    AbstractObjectDeclaration(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers, ImmutableList<TypeExpression<X>> extending, TypeBody<X> body) {
        super(annotations, modifiers, extending, body);
    }

}
