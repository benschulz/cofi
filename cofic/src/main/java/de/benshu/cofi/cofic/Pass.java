package de.benshu.cofi.cofic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import de.benshu.cofi.binary.deserialization.internal.AbstractBinaryModelContext;
import de.benshu.cofi.cofic.frontend.GenericModelData;
import de.benshu.cofi.cofic.frontend.companions.CompanionData;
import de.benshu.cofi.cofic.frontend.constraints.ConstraintsData;
import de.benshu.cofi.cofic.frontend.discovery.DiscoveryData;
import de.benshu.cofi.cofic.frontend.implementations.ImplementationData;
import de.benshu.cofi.cofic.frontend.interfaces.InterfaceData;
import de.benshu.cofi.cofic.frontend.namespace.AbstractResolution;
import de.benshu.cofi.cofic.model.binary.BinaryModule;
import de.benshu.cofi.cofic.notes.PrintStreamNotes;
import de.benshu.cofi.cofic.notes.async.Checker;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.ClassDeclaration;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.model.impl.MemberDeclarationImpl;
import de.benshu.cofi.model.impl.ModelContext;
import de.benshu.cofi.model.impl.NameExpression;
import de.benshu.cofi.model.impl.NameImpl;
import de.benshu.cofi.model.impl.ObjectDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.cofi.model.impl.Statement;
import de.benshu.cofi.model.impl.TraitDeclaration;
import de.benshu.cofi.model.impl.TypeExpression;
import de.benshu.cofi.model.impl.TypeParameterized;
import de.benshu.cofi.model.impl.TypeParameters;
import de.benshu.cofi.model.impl.UnionDeclaration;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import de.benshu.cofi.types.impl.unions.AbstractUnionTypeConstructor;
import de.benshu.commons.core.Optional;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.commons.core.streams.Collectors.map;

public final class Pass extends AbstractBinaryModelContext<Pass> implements ModelContext<Pass> {
    private final Fqn moduleFqn;
    private final ImmutableSet<BinaryModule> binaryDependencies;
    private final TypeSystemImpl<Pass> typeSystem;

    // ModuleGlueTyper
    private final Map<Fqn, PackageObjectDeclaration<Pass>> packageObjectDeclarations = Maps.newConcurrentMap();
    private ImmutableMap<Fqn, TemplateTypeConstructorMixin<Pass>> glueTypes;
    private ImmutableSetMultimap<PackageObjectDeclaration<Pass>, AbstractTypeDeclaration<Pass>> topLevelDeclarations;

    private CompanionData companionData;
    private DiscoveryData discoveryData;
    private ConstraintsData constraintsData;
    private InterfaceData interfaceData;
    private ImplementationData implementationData;

    public Pass(Fqn moduleFqn,
                ImmutableSet<BinaryModule> binaryDependencies,
                Function<Pass, TypeSystemImpl<Pass>> createTypeSystem) {

        this.moduleFqn = moduleFqn;
        this.binaryDependencies = binaryDependencies;
        this.typeSystem = createTypeSystem.apply(this);
    }

    @Override
    protected Pass self() {
        return this;
    }

    @Override
    public TypeSystemImpl<Pass> getTypeSystem() {
        return typeSystem;
    }

    @Override
    public Checker getChecker() {
        return check -> ForkJoinPool.commonPool().execute(() -> {
            check.check().forEach(PrintStreamNotes.err()::attach);
        });
    }

    public void setCompanionData(CompanionData companionData) {
        this.companionData = companionData;
    }

    public void setDiscoveryData(DiscoveryData discoveryData) {
        this.discoveryData = discoveryData;
    }

    void setConstraintsData(ConstraintsData constraintsData) {
        this.constraintsData = constraintsData;
    }

    void setInterfaceData(InterfaceData interfaceData) {
        this.interfaceData = interfaceData;
    }

    void setImplementationData(ImplementationData implementationData) {
        this.implementationData = implementationData;
    }

    public GenericModelData getGenericModelData() {
        return implementationData == null
                ? interfaceData == null ? constraintsData : interfaceData
                : implementationData;
    }

    public ImmutableMap<Fqn, TemplateTypeConstructorMixin<Pass>> getDependencyTypes() {
        return binaryDependencies.stream()
                .map(d -> immutableEntry(d.getFqn(), (TemplateTypeConstructorMixin<Pass>) resolveQualifiedTypeName(d.getFqn())))
                .collect(map());
    }

    public void defineGlueTypes(ImmutableMap<Fqn, TemplateTypeConstructorMixin<Pass>> glueTypes) {
        this.glueTypes = glueTypes;
    }

    public ImmutableMap<Fqn, TemplateTypeConstructorMixin<Pass>> getGlueTypes() {
        return glueTypes;
    }

    public void addPackageObjectDeclarations(ImmutableMap<Fqn, PackageObjectDeclaration<Pass>> packageObjectDeclarations) {
        this.packageObjectDeclarations.putAll(packageObjectDeclarations);
    }

    public PackageObjectDeclaration<Pass> lookUpPackageObjectDeclarationOf(Fqn fullyQualifiedName) {
        return tryLookUpPackageObjectDeclarationOf(fullyQualifiedName).get();
    }

    public Optional<PackageObjectDeclaration<Pass>> tryLookUpPackageObjectDeclarationOf(Fqn fullyQualifiedName) {
        return Optional.from(packageObjectDeclarations.get(fullyQualifiedName));
    }

    public void defineTopLevelDeclarations(ImmutableSetMultimap<PackageObjectDeclaration<Pass>, AbstractTypeDeclaration<Pass>> topLevelDeclarations) {
        this.topLevelDeclarations = topLevelDeclarations;
    }

    public ImmutableSet<AbstractTypeDeclaration<Pass>> lookUpTopLevelDeclarationIn(PackageObjectDeclaration<Pass> packageObjectDeclaration) {
        return topLevelDeclarations.get(packageObjectDeclaration);
    }

    public boolean isCompanion(AbstractTypeDeclaration<Pass> typeDeclaration) {
        return !tryLookUpAccompaniedBy(typeDeclaration).asSet().isEmpty();
    }

    public AbstractTypeDeclaration<Pass> lookUpAccompaniedBy(AbstractTypeDeclaration<Pass> companion) {
        return tryLookUpAccompaniedBy(companion).get();
    }

    public Optional<AbstractTypeDeclaration<Pass>> tryLookUpAccompaniedBy(AbstractTypeDeclaration<Pass> companion) {
        return Optional.from(companionData.companions.inverse().get(companion));
    }

    public ObjectDeclaration<Pass> lookUpCompanionObjectOf(AbstractTypeDeclaration<Pass> typeDeclaration) {
        return tryLookUpCompanionOf(typeDeclaration).get();
    }

    @Override
    public Optional<ObjectDeclaration<Pass>> tryLookUpCompanionOf(AbstractTypeDeclaration<Pass> typeDeclaration) {
        return Optional.from(companionData.companions.get(typeDeclaration));
    }

    @Override
    public TypeMixin<Pass, ?> lookUpTypeOf(TypeExpression<Pass> typeExpression) {
        final TypeMixin<Pass, ?> type = getGenericModelData().typeExpressionTypes.get(typeExpression);
        checkState(type != null);
        return type;
    }

    @Override
    public AbstractConstraints<Pass> lookUpConstraintsOf(TypeParameters<Pass> typeParameters) {
        final AbstractConstraints<Pass> constraints = constraintsData.typeParameterConstraints.get(typeParameters);
        checkState(constraints != null);
        return constraints;
    }

    public TemplateTypeConstructorMixin<Pass> lookUpTypeOf(ClassDeclaration<Pass> classDeclaration) {
        return (TemplateTypeConstructorMixin<Pass>) lookUpTypeOf((AbstractTypeDeclaration<Pass>) classDeclaration);
    }

    public TemplateTypeConstructorMixin<Pass> lookUpTypeOf(ObjectDeclaration<Pass> classDeclaration) {
        return (TemplateTypeConstructorMixin<Pass>) lookUpTypeOf((AbstractTypeDeclaration<Pass>) classDeclaration);
    }

    public TemplateTypeConstructorMixin<Pass> lookUpTypeOf(PackageObjectDeclaration<Pass> classDeclaration) {
        return (TemplateTypeConstructorMixin<Pass>) lookUpTypeOf((AbstractTypeDeclaration<Pass>) classDeclaration);
    }

    public TemplateTypeConstructorMixin<Pass> lookUpTypeOf(TraitDeclaration<Pass> classDeclaration) {
        return (TemplateTypeConstructorMixin<Pass>) lookUpTypeOf((AbstractTypeDeclaration<Pass>) classDeclaration);
    }

    public AbstractUnionTypeConstructor<Pass> lookUpTypeOf(UnionDeclaration<Pass> classDeclaration) {
        return (AbstractUnionTypeConstructor<Pass>) lookUpTypeOf((AbstractTypeDeclaration<Pass>) classDeclaration);
    }

    @Override
    public ProperTypeConstructorMixin<Pass, ?, ?> lookUpTypeOf(AbstractTypeDeclaration<Pass> typeDeclaration) {
        final ProperTypeConstructorMixin<Pass, ?, ?> type = discoveryData.types.get(typeDeclaration);
        checkState(type != null);
        return type;
    }

    @Override
    public TypeParameterListImpl<Pass> lookUpTypeParametersOf(TypeParameterized<Pass> typeParameterized) {
        final TypeParameterListImpl<Pass> typeParameters = discoveryData.typeParameters.get(typeParameterized);
        checkState(typeParameters != null);
        return typeParameters;
    }

    @Override
    public Fqn lookUpFqnOf(AbstractTypeDeclaration<Pass> typeDeclaration) {
        final Fqn fqn = interfaceData.typeDeclarationFqns.get(typeDeclaration);
        checkState(fqn != null);
        return fqn;
    }

    public AbstractTypeDeclaration<Pass> lookUpContainerOf(MemberDeclarationImpl<Pass> memberDeclaration) {
        final AbstractTypeDeclaration<Pass> container = interfaceData.containers.get(memberDeclaration);
        checkState(container != null);
        return container;
    }

    @Override
    public SourceMemberDescriptors<Pass> lookUpMemberDescriptorsOf(AbstractTypeDeclaration<Pass> typeDeclaration) {
        final SourceMemberDescriptors<Pass> sourceMemberDescriptors = interfaceData.memberDescriptors.get(typeDeclaration);
        return sourceMemberDescriptors == null
                ? SourceMemberDescriptors.empty()
                : sourceMemberDescriptors;
    }

    public AbstractResolution lookUpResolutionOf(NameExpression<Pass> nameExpression) {
        final AbstractResolution resolution = implementationData.nameResolutions.get(nameExpression);
        checkState(resolution != null);
        return resolution;
    }

    public AbstractTypeList<Pass, ?> lookUpTypeArgumentsTo(NameImpl<Pass> name) {
        final AbstractTypeList<Pass, ?> typeArguments = implementationData.nameTypeArguments.get(name);
        checkState(typeArguments != null);
        return typeArguments;
    }

    @Override
    public ProperTypeMixin<Pass, ?> lookUpTypeOf(ExpressionNode<Pass> expression) {
        final ProperTypeMixin<Pass, ?> type = implementationData.expressionTypes.get(expression);
        checkState(type != null);
        return type;
    }

    public Statement<Pass> lookUpTransformationOf(Statement<Pass> untransformed) {
        final Statement<Pass> transformed = implementationData.statementTransformations.get(untransformed);
        checkState(transformed != null);
        return transformed;
    }

    public ExpressionNode<Pass> lookUpTransformationOf(ExpressionNode<Pass> untransformed) {
        final ExpressionNode<Pass> transformed = implementationData.expressionTransformations.get(untransformed);
        checkState(transformed != null);
        return transformed;
    }

    @Override
    public ProperTypeConstructorMixin<Pass, ?, ?> resolveQualifiedTypeName(Fqn fqn) {
        return tryResolveQualifiedTypeName(fqn).get();
    }

    public Optional<ProperTypeConstructorMixin<Pass, ?, ?>> tryResolveQualifiedTypeName(Fqn fqn) {
        final java.util.Optional<BinaryModule> dependency = binaryDependencies.stream()
                .filter(e -> e.getFqn().contains(fqn))
                .sorted((a, b) -> -a.getFqn().compareTo(b.getFqn()))
                .findFirst();

        return Optional.from(dependency
                .map(d -> resolveTypeInModule(d, d.getFqn().getRelativeNameOf(fqn)))
                .orElseGet(() -> resolveInModule(fqn)));
    }

    private java.util.Optional<ProperTypeConstructorMixin<Pass, ?, ?>> resolveInModule(Fqn fqn) {
        if (!moduleFqn.contains(fqn))
            return java.util.Optional.empty();

        Map.Entry<PackageObjectDeclaration<Pass>, ImmutableList<String>> pkgAndRemaining = fqn.getAncestry().stream()
                .sorted(Comparator.<Fqn>naturalOrder().reversed())
                .map(c -> tryLookUpPackageObjectDeclarationOf(c).map(p -> immutableEntry(p, c.getRelativeNameOf(fqn))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst().get();

        PackageObjectDeclaration<Pass> pkg = pkgAndRemaining.getKey();
        ImmutableList<String> remaining = pkgAndRemaining.getValue();

        if (remaining.isEmpty())
            return java.util.Optional.of(lookUpTypeOf(pkg));

        final ImmutableSet<AbstractTypeDeclaration<Pass>> tlds = this.lookUpTopLevelDeclarationIn(pkg);

        final String n0 = remaining.get(0);
        java.util.Optional<ObjectDeclaration<Pass>> result = tlds.stream()
                .filter(d -> d instanceof ObjectDeclaration<?> && d.getName().equals(n0))
                .map(d -> (ObjectDeclaration<Pass>) d)
                .findFirst();

        remaining = remaining.subList(1, remaining.size());
        while (result.isPresent() && !remaining.isEmpty()) {
            final String n = remaining.get(0);
            result = result.get().body.elements.stream()
                    .filter(e -> e instanceof ObjectDeclaration<?>)
                    .map(e -> (ObjectDeclaration<Pass>) e)
                    .filter(d -> d.getName().equals(n))
                    .findFirst();

            remaining = remaining.subList(1, remaining.size());
        }

        return result
                .map(d -> tryLookUpAccompaniedBy(d).getOrReturn(d))
                .map(this::lookUpTypeOf);
    }
}
