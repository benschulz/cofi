package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.LocalVariableDeclaration;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;

class LocalVariableDeclarationNs extends AbstractNamespace {
    public static AbstractNamespace wrap(AbstractNamespace parent, LocalVariableDeclaration<Pass> localVariableDeclaration) {
        return new LocalVariableDeclarationNs(parent, localVariableDeclaration);
    }

    private final LocalVariableDeclaration<Pass> localVariableDeclaration;

    private LocalVariableDeclarationNs(AbstractNamespace parent, LocalVariableDeclaration<Pass> localVariableDeclaration) {
        super(parent);

        this.localVariableDeclaration = localVariableDeclaration;
    }

    @Override
    protected Optional<AbstractResolution> tryResolveLocally(LookUp lookUp, AbstractNamespace fromNamespace, String name) {
        if (localVariableDeclaration.name.getLexeme().equals(name)) {
            final ProperTypeMixin<Pass, ?> valueType = lookUp.lookUpProperTypeOf(localVariableDeclaration.type);
            final ProperTypeMixin<Pass, ?> variableType = lookUp.getTypeSystem().lookUp("Field").apply(AbstractTypeList.of(valueType));
            return some(new DefaultResolution(variableType));
        }

        return none();
    }
}
