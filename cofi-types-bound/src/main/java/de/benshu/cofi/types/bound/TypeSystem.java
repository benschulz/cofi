package de.benshu.cofi.types.bound;

public interface TypeSystem<X> {
    Bottom<X, ?> getBottom();

    TemplateTypeConstructor<X, ?, ?> getFunction();

    TemplateTypeConstructor<X, ?, ?> getMetaType();

    TemplateType<X, ?, ?> getTop();

    TemplateTypeConstructor<X, ?, ?> getTuple();

    TemplateTypeConstructor<X, ?, ?> getTuple(int arity);

    TemplateType<X, ?, ?> getUnit();

    Type<X, ?> lookUp(String name);
}