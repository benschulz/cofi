package de.benshu.cofi.types.bound;

import de.benshu.cofi.types.Variance;

public interface TypeParameter<X> {

    TypeParameterList<X> getList();

    TypeVariable<X, ?> getVariable();

    Variance getVariance();

    int getIndex();

}
