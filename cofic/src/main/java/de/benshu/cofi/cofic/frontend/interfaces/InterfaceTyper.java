package de.benshu.cofi.cofic.frontend.interfaces;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.Implicits;
import de.benshu.cofi.cofic.frontend.namespace.NamespaceTrackingVisitor;
import de.benshu.cofi.cofic.frontend.namespace.TypeParametersNs;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.Modifier;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.cofi.model.impl.MethodDeclarationImpl;
import de.benshu.cofi.model.impl.ObjectDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.cofi.model.impl.ParameterImpl;
import de.benshu.cofi.model.impl.PropertyDeclaration;
import de.benshu.cofi.model.impl.Statement;
import de.benshu.cofi.model.impl.TypeExpression;
import de.benshu.cofi.model.impl.TypeParamDecl;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.streams.Collectors;

import java.util.stream.Stream;

public class InterfaceTyper {
    public static InterfaceData type(Pass pass, ImmutableSet<CompilationUnit<Pass>> compilationUnits) {
        return compilationUnits
                .parallelStream()
                .map(u -> new Visitor(pass).visit(u, InterfaceData.builder(pass)))
                .collect(() -> InterfaceData.builder(pass), InterfaceDataBuilder::addAll, InterfaceDataBuilder::addAll)
                .addAll(new InterfaceDataBuilder(pass, pass.getGenericModelData()))
                .build();
    }

    private static final class Visitor extends NamespaceTrackingVisitor<InterfaceDataBuilder> {
        private final Pass pass;

        public Visitor(Pass pass) {
            super(pass);

            this.pass = pass;
        }

        @Override
        public InterfaceDataBuilder visitMethodDeclaration(MethodDeclarationImpl<Pass> methodDeclaration, InterfaceDataBuilder aggregate) {
            aggregate.defineContainer(methodDeclaration, getContainingTypeDeclaration());

            final Implicits.Builder implicits = Implicits.builder();
            final ImmutableList.Builder<ImmutableList<SourceType<Pass>>> builder = ImmutableList.builder();
            for (MethodDeclarationImpl.Piece<Pass> piece : methodDeclaration.pieces) {
                pushNs(TypeParametersNs.wrap(getNs(), piece));
                visit(piece.typeParameters, aggregate);

                visitAll(piece.params, aggregate);
                Stream<? extends ProperTypeMixin<Pass, ?>> types = piece.params.stream().map(p -> aggregate.lookUpProperTypeOf(p.type));
                builder.add(types.map(SourceType::of).collect(Collectors.list()));

                int tpImplicits = 0;
                for (TypeParamDecl<Pass> tpd : piece.typeParameters.declarations) {
                    if (tpd.implicit) {
                        ++tpImplicits;
                    } else {
                        break;
                    }
                }

                int i = 0;
                nextParam:
                for (; i < piece.params.size(); ++i) {
                    for (Modifier<Pass> m : piece.params.get(i).modifiers) {
                        if (m.getKind() == Modifier.Kind.IMPLICIT) {
                            continue nextParam;
                        }
                    }
                    break;
                }
                implicits.add(tpImplicits, i);
            }

            visit(methodDeclaration.returnType, aggregate);

            final TypeParameterListImpl<Pass> typeParams = pass.lookUpTypeParametersOf(methodDeclaration.pieces.get(0));
            final ImmutableList<ImmutableList<SourceType<Pass>>> params = builder.build();
            final SourceType<Pass> returnType = SourceType.of(aggregate.lookUpTypeOf(methodDeclaration.returnType));

            IndividualTags tags = IndividualTags.of(Implicits.TAG, implicits.build());

            final SourceMethodSignatureDescriptorImpl msd = new SourceMethodSignatureDescriptorImpl(typeParams, params, returnType, tags);

            aggregate.addMethodSignature(getContainingTypeDeclaration(), methodDeclaration.getName(), msd);

            for (MethodDeclarationImpl.Piece<Pass> piece : methodDeclaration.pieces.reverse())
                popNs();

            return aggregate;
        }

        @Override
        public InterfaceDataBuilder visitMethodDeclarationPiece(MethodDeclarationImpl.Piece<Pass> piece, InterfaceDataBuilder aggregate) {
            visit(piece.typeParameters, aggregate);
            visitAll(piece.params, aggregate);
            return aggregate;
        }

        @Override
        public InterfaceDataBuilder visitParameter(ParameterImpl<Pass> parameter, InterfaceDataBuilder aggregate) {
            visitAll(parameter.annotations, aggregate);
            visit(parameter.type, aggregate);
            return aggregate;
        }

        @Override
        public InterfaceDataBuilder visitPropertyDeclaration(PropertyDeclaration<Pass> propertyDeclaration, InterfaceDataBuilder aggregate) {
            aggregate.defineContainer(propertyDeclaration, getContainingTypeDeclaration());

            visitAll(propertyDeclaration.annotations, aggregate);
            visitAll(propertyDeclaration.modifiers, aggregate);
            visitAll(propertyDeclaration.traits, aggregate);
            visit(propertyDeclaration.type, aggregate);

            final ImmutableList.Builder<SourceType<Pass>> traitsBuilder = ImmutableList.builder();
            for (TypeExpression<Pass> trait : propertyDeclaration.traits) {
                traitsBuilder.add(SourceType.of(aggregate.lookUpTypeOf(trait)));
            }

            final ImmutableList<SourceType<Pass>> traits = traitsBuilder.build();
            final AbstractTypeDeclaration<Pass> owner = getContainingTypeDeclaration();

            aggregate.addProperty(owner, new SourcePropertyDescriptorImpl(pass, traits, propertyDeclaration, owner));

            return aggregate;
        }

        @Override
        protected InterfaceDataBuilder visitStatements(ImmutableList<Statement<Pass>> statements, InterfaceDataBuilder aggregate) {
            return aggregate;
        }

        @Override
        protected InterfaceDataBuilder visitTypeDeclaration(AbstractTypeDeclaration<Pass> typeDeclaration, InterfaceDataBuilder aggregate) {
            // TODO These instanceof checks aren't pretty.. use the relevant visit-methods
            if (typeDeclaration instanceof PackageObjectDeclaration<?>)
                aggregate.defineFqnOf(typeDeclaration, getNs().getContainingEntityFqn());
            else
                aggregate.defineFqnOf(typeDeclaration, getNs().getContainingEntityFqn().getChild(typeDeclaration.getName()));

            if (typeDeclaration instanceof PackageObjectDeclaration<?>) {
                final Fqn packageFqn = getNs().getPackageFqn();

                for (PackageObjectDeclaration<Pass> containingPackage : pass.tryLookUpPackageObjectDeclarationOf(packageFqn.getParent())) {
                    final SourceTypeDescriptorImpl descriptor = new SourceTypeDescriptorImpl(typeDeclaration, packageFqn.getLocalName(), IndividualTags.empty());

                    aggregate.addType(containingPackage, descriptor);
                }
            } else if (typeDeclaration instanceof ObjectDeclaration<?>) {
                final SourceTypeDescriptorImpl descriptor = new SourceTypeDescriptorImpl(typeDeclaration, typeDeclaration.getName(), IndividualTags.empty());

                aggregate.addType(getContainingTypeDeclaration(), descriptor);
            }

            return super.visitTypeDeclaration(typeDeclaration, aggregate);
        }
    }
}
