package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.AstNodeConstructorMethod;

import java.util.stream.Stream;

public class CompilationUnit<X extends ModelContext<X>> extends AbstractModelNode<X> implements TypeDeclarationContainer<X> {
    public static class ModuleDeclaration<X extends ModelContext<X>> extends AbstractModelNode<X> {
        @AstNodeConstructorMethod
        public static <X extends ModelContext<X>> ModuleDeclaration<X> of(final FullyQualifiedName<X> name) {
            return new ModuleDeclaration<>(name);
        }

        public final FullyQualifiedName<X> name;

        private ModuleDeclaration(final FullyQualifiedName<X> name) {
            this.name = name;
        }

        @Override
        public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
            return visitor.visitCompilationUnitModuleDeclaration(this, aggregate);
        }

        @Override
        public <N, L extends N, D extends L, S extends N, E extends N, T extends N> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
            return transformer.transformCompilationUnitModuleDeclaration(this);
        }
    }

    public static class PackageDeclaration<X extends ModelContext<X>> extends AbstractModelNode<X> {
        @AstNodeConstructorMethod
        public static <X extends ModelContext<X>> PackageDeclaration<X> of(final FullyQualifiedName<X> name) {
            return new PackageDeclaration<>(name);
        }

        public final FullyQualifiedName<X> name;

        private PackageDeclaration(final FullyQualifiedName<X> name) {
            this.name = name;
        }

        @Override
        public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
            return visitor.visitCompilationUnitPackageDeclaration(this, aggregate);
        }

        @Override
        public <N, L extends N, D extends L, S extends N, E extends N, T extends N> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
            return transformer.transformCompilationUnitPackageDeclaration(this);
        }
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> CompilationUnit<X> of(ModuleDeclaration<X> mod, PackageDeclaration<X> pkg, ImmutableList<ImportStatement<X>> imports, ImmutableList<AbstractTypeDeclaration<X>> declarations) {
        return new CompilationUnit<>(mod, pkg, imports, declarations);
    }

    public final ModuleDeclaration<X> moduleDeclaration;
    public final PackageDeclaration<X> packageDeclaration;
    public final ImmutableList<ImportStatement<X>> imports;
    public final ImmutableList<AbstractTypeDeclaration<X>> declarations;

    private CompilationUnit(ModuleDeclaration<X> mod, PackageDeclaration<X> pkg, ImmutableList<ImportStatement<X>> imports, ImmutableList<AbstractTypeDeclaration<X>> declarations) {
        this.moduleDeclaration = mod;
        this.packageDeclaration = pkg;
        this.imports = imports;
        this.declarations = declarations;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitCompilationUnit(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformCompilationUnit(this);
    }

    @Override
    public Stream<AbstractTypeDeclaration<X>> getTypeDeclarations() {
        return declarations.stream();
    }
}
