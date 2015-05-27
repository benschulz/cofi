package de.benshu.cofi.types;

public interface TypeSystem {
    Bottom getBottom();

    TemplateTypeConstructor getFunction();

    TemplateTypeConstructor getMetaType();

    TemplateType getTop();

    TemplateTypeConstructor getTuple();

    TemplateTypeConstructor getTuple(int arity);

    TemplateType getUnit();

    Type lookUp(String name);
}