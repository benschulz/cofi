package de.benshu.cofi.model;

import com.google.common.collect.ImmutableSet;

public interface AnnotatedNode<X> extends ModelNode<X> {
    ImmutableSet<? extends Annotation<X>> getAnnotationsAndModifiers();
}
