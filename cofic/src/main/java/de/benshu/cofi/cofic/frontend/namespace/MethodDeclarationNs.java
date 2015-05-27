package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.MethodDeclarationImpl;
import de.benshu.cofi.model.impl.ParameterImpl;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;

class MethodDeclarationNs extends AbstractNamespace {
    public static AbstractNamespace wrap(AbstractNamespace parent, MethodDeclarationImpl<Pass> methodDeclaration) {
        return new MethodDeclarationNs(parent, methodDeclaration);
    }

    private final MethodDeclarationImpl<Pass> methodDeclaration;

    private MethodDeclarationNs(AbstractNamespace parent, MethodDeclarationImpl<Pass> methodDeclaration) {
        super(parent);

        this.methodDeclaration = methodDeclaration;
    }

    @Override
    public AbstractConstraints<Pass> getContextualConstraints() {
        final MethodDeclarationImpl.Piece<Pass> firstPiece = methodDeclaration.pieces.get(0);

        return firstPiece.typeParameters.declarations.isEmpty() // TODO This is a bad branch. Method declarations should all have proper (contextual) constraints.
                ? super.getContextualConstraints()
                : pass.lookUpTypeParametersOf(firstPiece).getConstraints();
    }

    @Override
    protected Optional<AbstractResolution> tryResolveLocally(AbstractNamespace fromNamespace, String name) {
        for (MethodDeclarationImpl.Piece<Pass> mdp : methodDeclaration.pieces)
            for (ParameterImpl<Pass> p : mdp.params)
                if (p.name.getLexeme().equals(name)) {
                    // TODO This is duplicated for ParameterNs/LocalVariableNs
                    final ProperTypeMixin<Pass, ?> valueType = aggregate.lookUpProperTypeOf(p.type);
                    final ProperTypeMixin<Pass, ?> variableType = pass.getTypeSystem().lookUp("Field").apply(AbstractTypeList.of(valueType));
                    return some(new DefaultResolution(variableType));
                }

        return none();
    }
}
