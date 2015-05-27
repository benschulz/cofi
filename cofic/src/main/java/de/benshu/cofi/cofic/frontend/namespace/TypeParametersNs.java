package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.notes.Source;
import de.benshu.cofi.model.impl.TypeParamDecl;
import de.benshu.cofi.model.impl.TypeParameterized;
import de.benshu.cofi.types.impl.TypeVariableImpl;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;

public class TypeParametersNs extends AbstractNamespace {
    public static AbstractNamespace wrap(AbstractNamespace parent,
                                         TypeParameterized<Pass> typeParameterized) {
        return new de.benshu.cofi.cofic.frontend.namespace.TypeParametersNs(parent, typeParameterized);
    }

    private final TypeParameterized<Pass> typeParameterized;

    private TypeParametersNs(AbstractNamespace parent,
                             TypeParameterized<Pass> typeParameterized) {
        super(parent);

        this.typeParameterized = typeParameterized;
    }

    @Override
    protected Optional<AbstractNamespace> tryResolveNamespaceLocally(String name, Source.Snippet src) {
        for (int i = 0; i < typeParameterized.getTypeParameters().declarations.size(); ++i) {
            final TypeParamDecl<Pass> parameter = typeParameterized.getTypeParameters().declarations.get(i);
            final TypeVariableImpl<Pass, ?> variable = pass.lookUpTypeParametersOf(typeParameterized).getVariables().get(i);

            if (parameter.name.getLexeme().equals(name))
                return some(EmptyNamespace.create(this, variable));
        }

        return none();
    }
}
