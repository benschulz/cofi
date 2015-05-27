/**
 *
 */
package de.benshu.cofi.model.impl;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.model.MemberDeclaration;
import de.benshu.cofi.model.impl.TypeBody.Element;
import de.benshu.jswizzle.copyable.Copyable;

public abstract class MemberDeclarationImpl<X extends ModelContext<X>> extends Element<X> implements MemberDeclaration<X> {
    @Copyable.Include
    public final ImmutableList<AnnotationImpl<X>> annotations;
    @Copyable.Include
    public final ImmutableList<ModifierImpl<X>> modifiers;

    private final ImmutableSet<AnnotationImpl<X>> allAnnotations;

    MemberDeclarationImpl(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers) {
        this.annotations = annotations;
        this.modifiers = modifiers;
        this.allAnnotations = FluentIterable.from(annotations).append(modifiers).toSet();
    }

    @Override
    public ImmutableSet<AnnotationImpl<X>> getAnnotationsAndModifiers() {
        return allAnnotations;
    }
}