package de.benshu.cofi.inference;

import de.benshu.cofi.types.bound.Constraints;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.constraints.Conjunction;
import de.benshu.cofi.types.impl.constraints.Disjunction;

public class Inferencer<X extends TypeSystemContext<X>> {

    public static <X extends TypeSystemContext<X>> Inferencer<X> create(TypeSystemImpl<X> langTypes, AbstractConstraints<X> cs) {
        return new Inferencer<>(langTypes, cs);
    }

    private final InternalInferencer<X> generic;
    private final InternalInferencer<X> specific;

    public Inferencer(TypeSystemImpl<X> langTypes, AbstractConstraints<X> cs) {
        if (cs instanceof Conjunction) {
            generic = new ConjunctionInferencer<>(langTypes, (Conjunction<X>) cs, Mode.GENERIC, null);
            specific = new ConjunctionInferencer<>(langTypes, (Conjunction<X>) cs, Mode.SPECIFIC, null);
        } else if (cs instanceof Constraints.None) {
            generic = (InternalInferencer<X>) NoneInferencer.INSTANCE;
            specific = (InternalInferencer<X>) NoneInferencer.INSTANCE;
        } else {
            generic = new DisjunctionInferencer<>(langTypes, (Disjunction<X>) cs, Mode.GENERIC);
            specific = new DisjunctionInferencer<>(langTypes, (Disjunction<X>) cs, Mode.SPECIFIC);
        }
    }

    public Iterable<AbstractTypeList<X, ?>> infer(X context, Mode mode) {
        switch (mode) {
            case GENERIC:
                return () -> generic.iterator(context);
            case SPECIFIC:
                return () -> specific.iterator(context);
            default:
                throw new IllegalArgumentException(String.valueOf(mode));
        }
    }

    public Iterable<AbstractTypeList<X, ?>> infer(X context) {
        return infer(context, Mode.GENERIC);
    }

    enum Mode {
        GENERIC,
        SPECIFIC;
    }
}
