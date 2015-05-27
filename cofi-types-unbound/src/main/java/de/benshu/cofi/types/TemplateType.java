package de.benshu.cofi.types;

public interface TemplateType extends ProperType, ConstructedType<TemplateTypeConstructor> {

    TypeList<? extends TemplateType> getSupertypes();

}
