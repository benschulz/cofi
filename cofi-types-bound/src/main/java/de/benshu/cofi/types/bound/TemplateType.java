package de.benshu.cofi.types.bound;

public interface TemplateType<X, S extends TemplateType<X, S, CS>, CS extends TemplateTypeConstructor<X, CS, S>>
        extends ProperType<X, S>, ConstructedType<X, S, CS> {

    TypeList<X, ? extends TemplateType<X, ?, ?>> getSupertypes();

    TypeList<X, ?> getArguments();
}
