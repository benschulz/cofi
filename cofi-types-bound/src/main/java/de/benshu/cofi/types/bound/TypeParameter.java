package de.benshu.cofi.types.bound;

import de.benshu.cofi.types.Variance;
import de.benshu.commons.core.Debuggable;

public interface TypeParameter<X> extends Debuggable {

    TypeParameterList<X> getList();

    TypeVariable<X, ?> getVariable();

    Variance getVariance();

    int getIndex();

}
