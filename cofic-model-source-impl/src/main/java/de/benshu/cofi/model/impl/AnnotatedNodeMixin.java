package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.model.AnnotatedNode;

public interface AnnotatedNodeMixin<X extends ModelContext<X>> extends ModelNodeMixin<X>, AnnotatedNode<X> {
    @Override
    ImmutableSet<AnnotationImpl<X>> getAnnotationsAndModifiers();
}
