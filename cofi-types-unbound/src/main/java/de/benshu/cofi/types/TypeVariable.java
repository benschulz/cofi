package de.benshu.cofi.types;

public interface TypeVariable extends Type {
    TypeParameterList getParameterList();

    TypeParameter getParameter();
}
