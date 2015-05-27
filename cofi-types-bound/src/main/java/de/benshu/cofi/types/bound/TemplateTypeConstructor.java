package de.benshu.cofi.types.bound;

public interface TemplateTypeConstructor<X, S extends TemplateTypeConstructor<X, S, T>, T extends TemplateType<X, T, S>>
        extends TypeConstructor<X, S, T> {

    TypeList<X, ? extends TypeConstructor<X, ?, ? extends TemplateType<X, ?, ?>>> getSupertypes();
}
