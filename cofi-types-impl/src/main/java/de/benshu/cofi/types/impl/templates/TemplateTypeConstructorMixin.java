package de.benshu.cofi.types.impl.templates;

import de.benshu.cofi.types.bound.TemplateTypeConstructor;
import de.benshu.cofi.types.impl.NamedProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

public interface TemplateTypeConstructorMixin<X extends TypeSystemContext<X>>
        extends NamedProperTypeConstructorMixin<X, TemplateTypeConstructorMixin<X>, TemplateTypeImpl<X>>,
                TemplateTypeConstructor<X, TemplateTypeConstructorMixin<X>, TemplateTypeImpl<X>> {

    static <X extends TypeSystemContext<X>> TemplateTypeConstructorMixin<X> rebind(de.benshu.cofi.types.TemplateTypeConstructor unbound) {
        return (TemplateTypeConstructorMixin<X>) ProperTypeConstructorMixin.rebind(unbound);
    }

    @Override
    AbstractTypeList<X, TemplateTypeConstructorMixin<X>> getSupertypes();

    @Override
    de.benshu.cofi.types.TemplateTypeConstructor unbind();
}
