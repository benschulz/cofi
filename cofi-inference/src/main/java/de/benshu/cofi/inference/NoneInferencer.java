package de.benshu.cofi.inference;

import com.google.common.collect.Iterators;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.SomeTypeSystemContext;

import java.util.Iterator;

final class NoneInferencer<X extends TypeSystemContext<X>> extends InternalInferencer<X> {
    public static final NoneInferencer<?> INSTANCE = new NoneInferencer<SomeTypeSystemContext>();

    @Override
    public Iterator<AbstractTypeList<X, ?>> iterator(X context) {
        return Iterators.singletonIterator(AbstractTypeList.empty());
    }
}
