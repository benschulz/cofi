package de.benshu.cofi.cofic.frontend.interfaces;

import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.GenericModelData;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.MemberDeclaration;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.TypeExpression;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptors;

public class InterfaceData extends GenericModelData {
    public static InterfaceDataBuilder builder(Pass pass) {
        return new InterfaceDataBuilder(pass, GenericModelData.empty());
    }

    public final ImmutableMap<AbstractTypeDeclaration<Pass>, Fqn> typeDeclarationFqns;
    public final ImmutableMap<MemberDeclaration<Pass>, AbstractTypeDeclaration<Pass>> containers;
    public final ImmutableMap<AbstractTypeDeclaration<Pass>, SourceMemberDescriptors<Pass>> memberDescriptors;

    InterfaceData(
            ImmutableMap<TypeExpression<Pass>, TypeMixin<Pass, ?>> typeExpressionTypes,
            ImmutableMap<AbstractTypeDeclaration<Pass>, Fqn> typeDeclarationFqns,
            ImmutableMap<MemberDeclaration<Pass>, AbstractTypeDeclaration<Pass>> containers,
            ImmutableMap<AbstractTypeDeclaration<Pass>, SourceMemberDescriptors<Pass>> memberDescriptors) {
        super(typeExpressionTypes);

        this.typeDeclarationFqns = typeDeclarationFqns;
        this.containers = containers;
        this.memberDescriptors = memberDescriptors;
    }
}
