package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.tags.IndividualTags;

public interface InterpretedMethodSignatureDescriptor<X extends TypeSystemContext<X>> {
    TypeParameterListImpl<X> getTypeParameters();

    ImmutableList<AbstractTypeList<X, ProperTypeMixin<X, ?>>> getParameterTypes();

    ProperTypeMixin<X, ?> getReturnType();

    IndividualTags getTags();
}
