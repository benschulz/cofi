package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.Variance;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Pair;

public interface TypeParameterListDeclaration<X extends TypeSystemContext<X>> {
    <O> O supplyParameters(X context, TypeParameterListImpl<X> bound, Interpreter<ImmutableList<Pair<Variance, IndividualTags>>, O> interpreter);

    <O> O supplyConstraints(X context, TypeParameterListImpl<X> bound, Interpreter<AbstractConstraints<X>, O> interpreter);
}
