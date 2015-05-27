package de.benshu.cofi.types.impl.lists;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeMixin;

class RegularTypeList<X extends TypeSystemContext<X>, E extends TypeMixin<X, ?>> extends AbstractTypeList<X, E> {
    RegularTypeList(ImmutableList<? extends E> types) {
        super(types);
    }
}
