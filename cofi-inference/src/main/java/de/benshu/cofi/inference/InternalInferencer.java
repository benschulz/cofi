package de.benshu.cofi.inference;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.constraints.Monosemous;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.constraints.Conjunction;
import de.benshu.cofi.types.impl.constraints.Disjunction;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

abstract class InternalInferencer<X extends TypeSystemContext<X>> {
    static <X extends TypeSystemContext<X>> ImmutableList<InternalInferencer<X>> map(TypeSystemImpl<X> langTypes, ImmutableList<Monosemous<X>> cs, Inferencer.Mode mode, ProperTypeMixin<X, ?>[] given) {
        final ImmutableList.Builder<InternalInferencer<X>> builder = ImmutableList.builder();

        for (Monosemous<X> c : cs) {
            builder.add(new ConjunctionInferencer<>(langTypes, c, mode, given));
        }

        return builder.build();
    }

    static <X extends TypeSystemContext<X>> ImmutableList<InternalInferencer<X>> inferencers(TypeSystemImpl<X> langTypes, AbstractConstraints<X> cs, Inferencer.Mode mode, ProperTypeMixin<X, ?>[] given) {
        if (cs.isAll()) {
            return ImmutableList.of((InternalInferencer<X>) AllInferencer.INSTANCE);
        } else if (cs.isDisjunctive()) {
            return map(langTypes, ((Disjunction<X>) cs).getOptions(), mode, given);
        } else {
            return ImmutableList.of(new ConjunctionInferencer<>(langTypes, (Conjunction<X>) cs, mode, given));
        }
    }

    abstract Iterator<AbstractTypeList<X, ?>> iterator(X context);

    Stream<AbstractTypeList<X, ?>> stream(X context) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(context), Spliterator.ORDERED), false);
    }
}
