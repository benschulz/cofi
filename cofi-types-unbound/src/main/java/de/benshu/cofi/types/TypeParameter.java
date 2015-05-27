package de.benshu.cofi.types;

public interface TypeParameter {

    TypeParameterList getList();

    TypeVariable getVariable();

    Variance getVariance();

    int getIndex();

}
