package de.benshu.cofi.cofic.frontend.namespace;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.GenericModelDataBuilder;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.cofi.model.impl.TypeExpression;
import de.benshu.cofi.model.impl.TypeParameterized;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import de.benshu.commons.core.Optional;

public class LookUp {
    private final Pass pass;
    private final GenericModelDataBuilder<?, ?> aggregate;

    public LookUp(Pass pass, GenericModelDataBuilder<?, ?> aggregate) {
        this.pass = pass;
        this.aggregate = aggregate;
    }

    public ImmutableMap<Fqn, TemplateTypeConstructorMixin<Pass>> getGlueTypes() {
        return pass.getGlueTypes();
    }

    public PackageObjectDeclaration<Pass> lookUpPackageObjectDeclarationOf(Fqn fullyQualifiedName) {
        return pass.lookUpPackageObjectDeclarationOf(fullyQualifiedName);
    }

    public Optional<PackageObjectDeclaration<Pass>> tryLookUpPackageObjectDeclarationOf(Fqn fullyQualifiedName) {
        return pass.tryLookUpPackageObjectDeclarationOf(fullyQualifiedName);
    }

    public ImmutableSet<AbstractTypeDeclaration<Pass>> lookUpTopLevelDeclarationIn(PackageObjectDeclaration<Pass> packageObjectDeclaration) {
        return pass.lookUpTopLevelDeclarationIn(packageObjectDeclaration);
    }

    public TypeSystemImpl<Pass> getTypeSystem() {
        return pass.getTypeSystem();
    }

    public ProperTypeConstructorMixin<Pass, ?, ?> lookUpTypeOf(AbstractTypeDeclaration<Pass> typeDeclaration) {
        return pass.lookUpTypeOf(typeDeclaration);
    }

    public Fqn lookUpFqnOf(AbstractTypeDeclaration<Pass> typeDeclaration) {
        return pass.lookUpFqnOf(typeDeclaration);
    }

    public ProperTypeMixin<Pass, ?> lookUpProperTypeOf(TypeExpression<Pass> type) {
        return aggregate.lookUpProperTypeOf(type);
    }

    public TypeParameterListImpl<Pass> lookUpTypeParametersOf(TypeParameterized<Pass> typeParameterized) {
        return pass.lookUpTypeParametersOf(typeParameterized);
    }

    public Optional<TypeMixin<Pass, ?>> tryLookUpLangType(ImmutableList<String> names) {
        return pass.tryLookUpLangType(names);
    }

    public Optional<AbstractMember<Pass>> tryLookUpLangMember(String name) {
        return pass.tryLookUpLangMember(name);
    }
}
