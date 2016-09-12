package de.benshu.cofi.cofic.frontend.glue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.ModuleObjectDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;

public class ModuleGlueData {
    public final ModuleObjectDeclaration<Pass> moduleObjectDeclaration;
    public final ImmutableMap<Fqn, PackageObjectDeclaration<Pass>> packageObjectDeclarations;
    public final ImmutableSetMultimap<Fqn, AbstractTypeDeclaration<Pass>> topLevelDeclarations;
    public final ImmutableMap<Fqn, TemplateTypeConstructorMixin<Pass>> glueTypes;

    public ModuleGlueData(ModuleObjectDeclaration<Pass> moduleObjectDeclaration, ImmutableMap<Fqn, PackageObjectDeclaration<Pass>> packageObjectDeclarations, ImmutableSetMultimap<Fqn, AbstractTypeDeclaration<Pass>> topLevelDeclarations, ImmutableMap<Fqn, TemplateTypeConstructorMixin<Pass>> glueTypes) {
        this.moduleObjectDeclaration = moduleObjectDeclaration;
        this.packageObjectDeclarations = packageObjectDeclarations;
        this.topLevelDeclarations = topLevelDeclarations;
        this.glueTypes = glueTypes;
    }
}
