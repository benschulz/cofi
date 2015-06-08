package de.benshu.cofi.cofic.frontend.discovery;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.AnnotationImpl;
import de.benshu.cofi.model.impl.ClassDeclaration;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.cofi.model.impl.MethodDeclarationImpl;
import de.benshu.cofi.model.impl.ModuleObjectDeclaration;
import de.benshu.cofi.model.impl.ObjectDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.cofi.model.impl.PropertyDeclaration;
import de.benshu.cofi.model.impl.TraitDeclaration;
import de.benshu.cofi.model.impl.TraversingModelVisitor;
import de.benshu.cofi.model.impl.UnionDeclaration;

public class Discoverer {
    public static DiscoveryData discover(Pass pass, ImmutableSet<CompilationUnit<Pass>> compilationUnits) {
        return compilationUnits
                .parallelStream()
                .map(u -> new Visitor(pass).visit(u, DiscoveryData.builder()))
                .collect(DiscoveryData::builder, DiscoveryDataBuilder::addAll, DiscoveryDataBuilder::addAll)
                .build();
    }

    private static final class Visitor extends TraversingModelVisitor<Pass, DiscoveryDataBuilder> {
        private final Pass pass;

        public Visitor(Pass pass) {
            this.pass = pass;
        }

        @Override
        public DiscoveryDataBuilder visitAnnotation(AnnotationImpl<Pass> annotation, DiscoveryDataBuilder aggregate) {
            return aggregate;
        }

        @Override
        public DiscoveryDataBuilder visitClassDeclaration(ClassDeclaration<Pass> classDeclaration, DiscoveryDataBuilder aggregate) {
            visitTypeDeclaration(classDeclaration, aggregate);
            return super.visitClassDeclaration(classDeclaration, aggregate);
        }

        @Override
        public DiscoveryDataBuilder visitMethodDeclaration(MethodDeclarationImpl<Pass> methodDeclaration, DiscoveryDataBuilder aggregate) {
            for (MethodDeclarationImpl.Piece<Pass> piece : methodDeclaration.pieces)
                aggregate.defineTypeParametersOf(piece, piece.typeParameters.bind(pass));
            return aggregate;
        }


        @Override
        public DiscoveryDataBuilder visitModuleObjectDeclaration(ModuleObjectDeclaration<Pass> moduleObjectDeclaration, DiscoveryDataBuilder aggregate) {
            visitTypeDeclaration(moduleObjectDeclaration, aggregate);
            return super.visitModuleObjectDeclaration(moduleObjectDeclaration, aggregate);
        }

        @Override
        public DiscoveryDataBuilder visitObjectDeclaration(ObjectDeclaration<Pass> objectDecl, DiscoveryDataBuilder aggregate) {
            visitTypeDeclaration(objectDecl, aggregate);
            return super.visitObjectDeclaration(objectDecl, aggregate);
        }

        @Override
        public DiscoveryDataBuilder visitPackageObjectDeclaration(PackageObjectDeclaration<Pass> packageObjectDeclaration, DiscoveryDataBuilder aggregate) {
            visitTypeDeclaration(packageObjectDeclaration, aggregate);
            return super.visitPackageObjectDeclaration(packageObjectDeclaration, aggregate);
        }

        @Override
        public DiscoveryDataBuilder visitPropertyDeclaration(PropertyDeclaration<Pass> propertyDeclaration, DiscoveryDataBuilder aggregate) {
            return aggregate;
        }

        @Override
        public DiscoveryDataBuilder visitTraitDeclaration(TraitDeclaration<Pass> traitDeclaration, DiscoveryDataBuilder aggregate) {
            visitTypeDeclaration(traitDeclaration, aggregate);
            return super.visitTraitDeclaration(traitDeclaration, aggregate);
        }

        @Override
        public DiscoveryDataBuilder visitUnionDeclaration(UnionDeclaration<Pass> unionDeclaration, DiscoveryDataBuilder aggregate) {
            visitTypeDeclaration(unionDeclaration, aggregate);
            return super.visitUnionDeclaration(unionDeclaration, aggregate);
        }

        private DiscoveryDataBuilder visitTypeDeclaration(AbstractTypeDeclaration<Pass> typeDeclaration, DiscoveryDataBuilder aggregate) {
            aggregate.defineTypeOf(typeDeclaration, typeDeclaration.bind(pass));

            // TODO would be nice to actually use the *same* TypeParameterListImpl
            for (AbstractTypeDeclaration<Pass> accompanied : pass.tryLookUpAccompaniedBy(typeDeclaration))
                return aggregate.defineTypeParametersOf(typeDeclaration, accompanied.getTypeParameters().bind(pass));
            return aggregate.defineTypeParametersOf(typeDeclaration, typeDeclaration.getTypeParameters().bind(pass));
        }
    }
}
