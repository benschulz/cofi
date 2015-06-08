package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import de.benshu.commons.core.Optional;

// TODO Some (most?) of these should not be here.
public interface ModelContext<X extends ModelContext<X>> extends TypeSystemContext<X> {
    ImmutableMap<Fqn, TemplateTypeConstructorMixin<X>> getGlueTypes();

    default ProperTypeMixin<X, ?> lookUpProperTypeOf(TypeExpression<X> type) {
        return (ProperTypeMixin<X, ?>) lookUpTypeOf(type);
    }

    TypeMixin<X, ?> lookUpTypeOf(TypeExpression<X> type);

    SourceMemberDescriptors<X> lookUpMemberDescriptorsOf(AbstractTypeDeclaration<X> typeDeclaration);

    AbstractConstraints<X> lookUpConstraintsOf(TypeParameters<X> typeParameters);

    ProperTypeMixin<X, ?> lookUpTypeOf(ExpressionNode<X> expression);

    boolean isCompanion(AbstractTypeDeclaration<X> typeDeclaration);

    Optional<ObjectDeclaration<X>> tryLookUpCompanionOf(AbstractTypeDeclaration<X> typeDeclaration);

    Fqn lookUpFqnOf(AbstractTypeDeclaration<X> typeDeclaration);

    ProperTypeConstructorMixin<X, ?, ?> lookUpTypeOf(AbstractTypeDeclaration<X> typeDeclaration);

    TypeParameterListImpl<X> lookUpTypeParametersOf(TypeParameterized<X> typeParameterized);

    Optional<AbstractTypeDeclaration<X>> tryLookUpAccompaniedBy(AbstractTypeDeclaration<X> companion);
}
