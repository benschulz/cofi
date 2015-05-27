package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.impl.declarations.TypeParameterListDeclaration;

import static com.google.common.base.Preconditions.checkArgument;

public class UnboundTypeParameterList<X extends TypeSystemContext<X>> {
    private final TypeParameterListDeclaration<X> declaration;

    public UnboundTypeParameterList(TypeParameterListDeclaration<X> declaration) {
        checkArgument(declaration != null);

        this.declaration = declaration;
    }

    public TypeParameterListDeclaration<X> getDeclaration() {
        return declaration;
    }

    public TypeParameterListImpl<X> bind(X context) {
        return new TypeParameterListImpl<>(this, context);
    }
}
