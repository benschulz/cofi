package de.benshu.cofi.types.impl.declarations;

import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

public interface InterpretedPropertyDescriptor<X extends TypeSystemContext<X>> extends InterpretedMemberDescriptor<X> {
    ProperTypeMixin<X, ?> getType();

    AbstractTypeList<X, AbstractTemplateTypeConstructor<X>> getTraits();
}
