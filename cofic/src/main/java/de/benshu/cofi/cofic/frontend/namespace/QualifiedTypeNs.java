package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.notes.Source;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.commons.core.Optional;

public class QualifiedTypeNs extends AbstractNamespace {
    public static QualifiedTypeNs create(Fqn typeName, ProperTypeConstructorMixin<Pass, ?, ?> type) {
        return new QualifiedTypeNs(typeName, type);
    }

    private final Fqn typeName;
    private final ProperTypeConstructorMixin<Pass, ?, ?> type;

    public QualifiedTypeNs(Fqn typeName, ProperTypeConstructorMixin<Pass, ?, ?> type) {
        this.typeName = typeName;
        this.type = type;
    }

    @Override
    TypeMixin<Pass, ?> asType(LookUp lookUp) {
        return type;
    }

    @Override
    ExpressionNode<Pass> getAccessor(LookUp lookUp) {
        return getAccessor(typeName);
    }

    @Override
    Optional<AbstractNamespace> tryResolveNamespaceLocally(LookUp lookUp, String name, Source.Snippet src) {
        final Fqn resolvedTypeName = typeName.getChild(name);
        return lookUp.tryResolveQualifiedTypeName(resolvedTypeName)
                .map(t -> create(resolvedTypeName, t));
    }

    @Override
    Optional<AbstractResolution> tryResolveLocally(LookUp lookUp, AbstractNamespace fromNamespace, String name) {
        return type.applyTrivially().lookupMember(name)
                .map(m -> new DefaultResolution(m.getType(), getAccessor(lookUp), m));
    }
}
