package de.benshu.cofi.cofic.frontend.companions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.AnnotationImpl;
import de.benshu.cofi.model.impl.ClassDeclaration;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.cofi.model.impl.FullyQualifiedName;
import de.benshu.cofi.model.impl.MethodDeclarationImpl;
import de.benshu.cofi.model.impl.ModelVisitor;
import de.benshu.cofi.model.impl.NamedTypeExpression;
import de.benshu.cofi.model.impl.ObjectDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.cofi.model.impl.RelativeNameImpl;
import de.benshu.cofi.model.impl.TraitDeclaration;
import de.benshu.cofi.model.impl.TypeBody;
import de.benshu.cofi.model.impl.TypeParameters;
import de.benshu.cofi.model.impl.UnionDeclaration;
import de.benshu.cofi.parser.lexer.ArtificialToken;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.commons.core.streams.Collectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static de.benshu.commons.core.streams.Collectors.list;
import static de.benshu.commons.core.streams.Collectors.set;

public class CompanionObjectNormalizer {
    public static ImmutableSet<CompilationUnit<Pass>> normalize(Pass pass, ImmutableSet<CompilationUnit<Pass>> rawCompilationUnits) {
        final CompanionObjectNormalizer companionObjectNormalizer = new CompanionObjectNormalizer(pass);

        // TODO Eww.. clean this up.
        final ImmutableList<Map.Entry<CompilationUnit<Pass>, CompanionDataBuilder>> data = rawCompilationUnits
                .parallelStream()
                .map(u -> {
                    final CompanionDataBuilder aggregate = new CompanionDataBuilder();
                    return Maps.immutableEntry(companionObjectNormalizer.normalize(u, aggregate), aggregate);
                })
                .collect(list());

        // TODO Double "eww"..
        pass.setCompanionData(data.stream().map(Map.Entry::getValue).reduce(new CompanionDataBuilder(), CompanionDataBuilder::addAll).build());

        return data.stream()
                .map(Map.Entry::getKey)
                .collect(set());
    }

    private final Pass pass;

    public CompanionObjectNormalizer(Pass pass) {
        this.pass = pass;
    }

    public CompilationUnit<Pass> normalize(CompilationUnit<Pass> compilationUnit, CompanionDataBuilder aggregate) {
        ImmutableListMultimap<String, ? extends AbstractTypeDeclaration<Pass>> typeDeclarationsByName = compilationUnit.getTypeDeclarationsByName();

        return CompilationUnit.of(
                compilationUnit.moduleDeclaration,
                compilationUnit.packageDeclaration,
                compilationUnit.imports,
                normalizeTypeDeclarationsIn(compilationUnit.declarations, typeDeclarationsByName, aggregate));
    }

    private <T> ImmutableList<T> normalizeTypeDeclarationsIn(
            ImmutableList<T> list,
            ImmutableListMultimap<String, ? extends AbstractTypeDeclaration<Pass>> typeDeclarationsByName,
            CompanionDataBuilder aggregate) {

        return list.stream().flatMap(e -> e instanceof AbstractTypeDeclaration<?>
                ? normalizeTypeDeclaration((AbstractTypeDeclaration<Pass>) e, typeDeclarationsByName, aggregate).map(x -> (T) x)
                : Stream.of(e))
                .collect(Collectors.list());
    }

    private Stream<AbstractTypeDeclaration<Pass>> normalizeTypeDeclaration(
            AbstractTypeDeclaration<Pass> typeDeclaration,
            ImmutableListMultimap<String, ? extends AbstractTypeDeclaration<Pass>> siblingsByName,
            CompanionDataBuilder aggregate) {

        return typeDeclaration.accept(new ModelVisitor<Pass, Stream<AbstractTypeDeclaration<Pass>>>() {
            @Override
            public Stream<AbstractTypeDeclaration<Pass>> visitClassDeclaration(ClassDeclaration<Pass> classDeclaration, Stream<AbstractTypeDeclaration<Pass>> unused) {
                return normalizeClassDeclaration(classDeclaration, siblingsByName, aggregate);
            }

            @Override
            public Stream<AbstractTypeDeclaration<Pass>> visitObjectDeclaration(ObjectDeclaration<Pass> objectDecl, Stream<AbstractTypeDeclaration<Pass>> unused) {
                return normalizeObjectDeclaration(objectDecl, siblingsByName, aggregate);
            }

            @Override
            public Stream<AbstractTypeDeclaration<Pass>> visitPackageObjectDeclaration(PackageObjectDeclaration<Pass> packageObjectDeclaration, Stream<AbstractTypeDeclaration<Pass>> unused) {
                return Stream.of(packageObjectDeclaration);
            }

            @Override
            public Stream<AbstractTypeDeclaration<Pass>> visitTraitDeclaration(TraitDeclaration<Pass> traitDeclaration, Stream<AbstractTypeDeclaration<Pass>> unused) {
                return normalizeGenericDeclaration(traitDeclaration, siblingsByName, aggregate);
            }

            @Override
            public Stream<AbstractTypeDeclaration<Pass>> visitUnionDeclaration(UnionDeclaration<Pass> unionDeclaration, Stream<AbstractTypeDeclaration<Pass>> unused) {
                return normalizeGenericDeclaration(unionDeclaration, siblingsByName, aggregate);
            }
        }, null);
    }

    private Stream<AbstractTypeDeclaration<Pass>> normalizeClassDeclaration(
            ClassDeclaration<Pass> classDeclaration,
            ImmutableListMultimap<String, ? extends AbstractTypeDeclaration<Pass>> siblingsByName,
            CompanionDataBuilder aggregate) {

        final ImmutableList<? extends AbstractTypeDeclaration<Pass>> potentialCompanions = siblingsByName.get(classDeclaration.getName());

        return potentialCompanions.stream().anyMatch(d -> d instanceof ObjectDeclaration<?>)
                ? Stream.of(classDeclaration)
                : declareCompanion(classDeclaration, synthesizeCompanionFor(classDeclaration), aggregate);
    }

    private ObjectDeclaration<Pass> synthesizeCompanionFor(ClassDeclaration<Pass> classDeclaration) {
        return synthesizeCompanionFor((AbstractTypeDeclaration<Pass>) classDeclaration)
                .withBody(TypeBody.of(ImmutableList.of(synthesizeFactoryMethodFor(classDeclaration))));
    }

    private Stream<AbstractTypeDeclaration<Pass>> normalizeObjectDeclaration(
            ObjectDeclaration<Pass> objectDeclaration,
            ImmutableListMultimap<String, ? extends AbstractTypeDeclaration<Pass>> siblingsByName,
            CompanionDataBuilder aggregate) {

        final ObjectDeclaration<Pass> normalized = objectDeclaration.withBody(TypeBody.of(
                normalizeTypeDeclarationsIn(
                        objectDeclaration.body.elements,
                        objectDeclaration.getTypeDeclarationsByName(), aggregate)));
        final Optional<? extends AbstractTypeDeclaration<Pass>> companion = siblingsByName.get(normalized.getName()).stream()
                .filter(d -> !(d instanceof ObjectDeclaration<?>))
                .findFirst();

        if (companion.isPresent()) {
            ObjectDeclaration<Pass> factoryNormalized = companion.get() instanceof ClassDeclaration<?>
                    ? augmentWithFactoryMethodFor(normalized, (ClassDeclaration<Pass>) companion.get())
                    : normalized;

            aggregate.defineCompanion(companion.get(), factoryNormalized);

            return Stream.of(factoryNormalized);
        }

        return Stream.of(normalized);
    }

    private ObjectDeclaration<Pass> augmentWithFactoryMethodFor(ObjectDeclaration<Pass> objectDeclaration, ClassDeclaration<Pass> classDeclaration) {
        return objectDeclaration.withBody(TypeBody.of(ImmutableList.copyOf(Iterables.concat(
                ImmutableList.of(synthesizeFactoryMethodFor(classDeclaration)),
                objectDeclaration.body.elements))));
    }

    private TypeBody.Element<Pass> synthesizeFactoryMethodFor(ClassDeclaration<Pass> classDeclaration) {
        final int beginLine = classDeclaration.id.getBeginLine();
        final int beginColumn = classDeclaration.id.getBeginColumn();
        final int endLine = classDeclaration.id.getEndLine();
        final int endColumn = classDeclaration.id.getEndColumn();

        return MethodDeclarationImpl.of(
                ImmutableList.of(AnnotationImpl.of(
                        NamedTypeExpression.<Pass>of2(FullyQualifiedName.create(
                                ArtificialToken.createString(classDeclaration.getId(), Token.Kind.IDENTIFIER,
                                        "cofi", "lang", "RuntimeImplemented").getTokens()
                        )),
                        null,
                        null
                )),
                ImmutableList.of(),
                ImmutableList.of(MethodDeclarationImpl.Piece.of(
                        ArtificialToken.create(beginLine, beginColumn, Token.Kind.IDENTIFIER, "create", endLine, endColumn),
                        TypeParameters.none(),
                        classDeclaration.parameters
                )),
                NamedTypeExpression.of2(RelativeNameImpl.of(
                        classDeclaration.id,
                        classDeclaration.typeParameters.declarations.stream()
                                .map(p -> NamedTypeExpression.of2(RelativeNameImpl.<Pass>of(p.name))).collect(Collectors.list())
                )),
                ImmutableList.of()
        );
    }

    private Stream<AbstractTypeDeclaration<Pass>> normalizeGenericDeclaration(
            AbstractTypeDeclaration<Pass> declaration,
            ImmutableListMultimap<String, ? extends AbstractTypeDeclaration<Pass>> siblingsByName,
            CompanionDataBuilder aggregate) {

        final ImmutableList<? extends AbstractTypeDeclaration<Pass>> potentialCompanions = siblingsByName.get(declaration.getName());

        return potentialCompanions.stream().anyMatch(d -> d instanceof ObjectDeclaration<?>)
                ? Stream.of(declaration)
                : declareCompanion(declaration, synthesizeCompanionFor(declaration), aggregate);
    }

    private Stream<AbstractTypeDeclaration<Pass>> declareCompanion(
            AbstractTypeDeclaration<Pass> declaration,
            ObjectDeclaration<Pass> companion,
            CompanionDataBuilder aggregate) {

        return Stream.of(aggregate.defineCompanion(declaration, companion), declaration);
    }

    private ObjectDeclaration<Pass> synthesizeCompanionFor(AbstractTypeDeclaration<Pass> typeDeclaration) {
        return ObjectDeclaration.<Pass>of(
                ImmutableList.of(),
                ImmutableList.of(),
                ArtificialToken.dupe(typeDeclaration.getId()),
                TypeParameters.none(),
                ImmutableList.of(),
                TypeBody.empty());
    }
}




































