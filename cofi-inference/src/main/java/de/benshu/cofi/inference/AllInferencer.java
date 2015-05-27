package de.benshu.cofi.inference;

import com.google.common.collect.Iterators;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.SomeTypeSystemContext;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

import java.util.Iterator;

final class AllInferencer<X extends TypeSystemContext<X>> extends InternalInferencer<X> {
    public static final AllInferencer<?> INSTANCE = new AllInferencer<SomeTypeSystemContext>();

    @Override
    public Iterator<AbstractTypeList<X, ?>> iterator(X context) {
        return Iterators.emptyIterator();
    }
}
