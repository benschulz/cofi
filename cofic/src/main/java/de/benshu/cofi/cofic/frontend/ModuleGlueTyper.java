package de.benshu.cofi.cofic.frontend;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.model.common.FullyQualifiedTypeName;
import de.benshu.cofi.cofic.model.common.TypeTags;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.declarations.source.SourceTypeDescriptor;
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Optional;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.commons.core.streams.Collectors.map;
import static de.benshu.commons.core.streams.Collectors.set;
import static de.benshu.commons.core.streams.Collectors.setMultimap;

public class ModuleGlueTyper {
    public static void type(Pass pass, Fqn moduleFqn, ImmutableSet<CompilationUnit<Pass>> compilationUnits) {
        final ImmutableMap.Builder<Fqn, PackageObjectDeclaration<Pass>> packageObjectDeclarationsBuilder = ImmutableMap.builder();
        final ImmutableSetMultimap.Builder<Fqn, AbstractTypeDeclaration<Pass>> topLevelDeclarationsBuilder = ImmutableSetMultimap.builder();

        compilationUnits.forEach(u -> {
            if (!moduleFqn.equals(u.moduleDeclaration.name.fqn))
                throw new AssertionError(); // TODO note an error
            if (!moduleFqn.contains(u.packageDeclaration.name.fqn))
                throw new AssertionError(); // TODO note an error

            u.declarations.stream()
                    .filter(d -> d instanceof PackageObjectDeclaration<?>)
                    .forEach(d -> packageObjectDeclarationsBuilder.put(u.packageDeclaration.name.fqn, (PackageObjectDeclaration<Pass>) d));

            u.declarations.stream()
                    .filter(d -> !(d instanceof PackageObjectDeclaration<?>))
                    .forEach(d -> topLevelDeclarationsBuilder.put(u.packageDeclaration.name.fqn, d));
        });

        final ImmutableMap<Fqn, PackageObjectDeclaration<Pass>> packageObjectDeclarations = packageObjectDeclarationsBuilder.build();
        pass.addPackageObjectDeclarations(packageObjectDeclarations);

        pass.defineTopLevelDeclarations(topLevelDeclarationsBuilder.build().entries().stream()
                .map(e -> immutableEntry(packageObjectDeclarations.get(e.getKey()), e.getValue()))
                .collect(setMultimap()));

        final PackageObjectDeclaration<Pass> module = packageObjectDeclarations.get(moduleFqn);

        final ImmutableMap<Fqn, Supplier<TemplateTypeConstructorMixin<Pass>>> dependencies = pass.getDependencyTypes().entrySet().stream()
                .map(d -> immutableEntry(d.getKey(), (Supplier<TemplateTypeConstructorMixin<Pass>>) d::getValue))
                .collect(map());

        // TODO this is broken w.r.t. nested modules
        final ImmutableSet<Fqn> glueObjectFqns = Stream.concat(
                Stream.of(moduleFqn.getParent()),
                dependencies.entrySet().stream().map(d -> d.getKey().getParent()))
                .flatMap(fqn -> fqn.getAncestry().stream())
                .distinct()
                .collect(set());

        final ImmutableMap<Fqn, TemplateTypeConstructorMixin<Pass>> glueTypes = glueObjectFqns.stream()
                .map(fqn -> immutableEntry(fqn, AbstractTemplateTypeConstructor.<Pass>create(
                        TemplateTypeDeclaration.memoizing(
                                x -> TypeParameterListImpl.empty(),
                                x -> ImmutableList.of(),
                                x -> {
                                    final Stream<Map.Entry<String, TemplateTypeConstructorMixin<Pass>>> containedDependencies = x.getDependencyTypes().entrySet().stream()
                                            .filter(m -> m.getKey().getParent().equals(fqn))
                                            .map(m -> immutableEntry(m.getKey().getLocalName(), m.getValue()));

                                    final Stream<Map.Entry<String, TemplateTypeConstructorMixin<Pass>>> containedModules = Stream.concat(
                                            Optional.some(moduleFqn)
                                                    .filter(n -> n.getParent().equals(fqn))
                                                    .map(n -> immutableEntry(n.getLocalName(), x.lookUpTypeOf(module)))
                                                    .stream(),
                                            containedDependencies
                                    );

                                    final Stream<Map.Entry<String, TemplateTypeConstructorMixin<Pass>>> containedGlueObjects = pass.getGlueTypes().entrySet().stream()
                                            .filter(e -> e.getKey().length() > 0)
                                            .filter(e -> e.getKey().getParent().equals(fqn))
                                            .map(e -> immutableEntry(e.getKey().getLocalName(), e.getValue()));

                                    return SourceMemberDescriptors.create(Stream.concat(containedModules, containedGlueObjects)
                                            .map(e -> new SourceTypeDescriptor<Pass>() {
                                                @Override
                                                public String getName() {
                                                    return e.getKey();
                                                }

                                                @Override
                                                public SourceType<Pass> getType(Pass context) {
                                                    return SourceType.of(e.getValue());
                                                }
                                            })
                                            .collect(set()));
                                },
                                x -> IndividualTags.of(TypeTags.NAME, FullyQualifiedTypeName.create(() -> fqn)))
                ).bind(pass)))
                .collect(map());

        pass.defineGlueTypes(glueTypes);
    }
}
