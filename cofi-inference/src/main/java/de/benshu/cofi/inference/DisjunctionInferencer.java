package de.benshu.cofi.inference;

import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.constraints.Disjunction;

import java.util.Iterator;

final class DisjunctionInferencer<X extends TypeSystemContext<X>> extends InternalInferencer<X> {
    private final TypeSystemImpl<X> langTypes;
    private final Disjunction<X> disjunction;
    private final Inferencer.Mode mode;

    public DisjunctionInferencer(TypeSystemImpl<X> langTypes, Disjunction<X> disjunction, Inferencer.Mode mode) {
        this.langTypes = langTypes;
        this.disjunction = disjunction;
        this.mode = mode;
    }

    @Override
    public Iterator<AbstractTypeList<X, ?>> iterator(X context) {
        return disjunction.getOptions().stream()
                .flatMap(o -> new ConjunctionInferencer<>(langTypes, o, mode, null).stream(context))
                .iterator();
    }
}
