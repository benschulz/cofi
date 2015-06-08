package de.benshu.cofi.model;

import com.google.common.collect.ImmutableList;

public interface MethodDeclaration<X> extends MemberDeclaration<X> {
    ImmutableList<? extends ImmutableList<? extends Parameter<X>>> getParameters();
}
