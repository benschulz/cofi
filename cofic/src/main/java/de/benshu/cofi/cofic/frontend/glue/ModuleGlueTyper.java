package de.benshu.cofi.cofic.frontend.glue;

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
import de.benshu.cofi.model.impl.ModuleObjectDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.declarations.source.SourceTypeDescriptor;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Optional;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.commons.core.streams.Collectors.listMultimap;
import static de.benshu.commons.core.streams.Collectors.map;
import static de.benshu.commons.core.streams.Collectors.set;
import static de.benshu.commons.core.streams.Collectors.setMultimap;
import static de.benshu.commons.core.streams.Collectors.single;

public class ModuleGlueTyper {
    public static ModuleGlueData type(Pass pass, Fqn moduleFqn, ImmutableSet<CompilationUnit<Pass>> compilationUnits) {
        final ImmutableMap<Fqn, ImmutableSetMultimap<Class<?>, AbstractTypeDeclaration<Pass>>> delcarationsByPackageAndClass = compilationUnits.stream()
                .flatMap(u -> u.declarations.stream().map(d -> immutableEntry(u.packageDeclaration.name.fqn, d)))
                .collect(listMultimap()).asMap().entrySet().stream()
                .<Map.Entry<Fqn, ImmutableSetMultimap<Class<?>, AbstractTypeDeclaration<Pass>>>>map(e -> immutableEntry(
                        e.getKey(),
                        e.getValue().stream()
                                .map(d -> immutableEntry(
                                        Stream.<Class<?>>of(ModuleObjectDeclaration.class, PackageObjectDeclaration.class)
                                                .filter(c -> c.isAssignableFrom(d.getClass()))
                                                .findAny().orElse(AbstractTypeDeclaration.class),
                                        d))
                                .collect(setMultimap())))
                .collect(map());

        final ModuleObjectDeclaration<Pass> moduleObjectDeclaration = delcarationsByPackageAndClass.values().stream()
                .flatMap(ds -> ds.get(ModuleObjectDeclaration.class).stream())
                .map(d -> (ModuleObjectDeclaration<Pass>) d)
                .collect(single());

        final ImmutableMap<Fqn, PackageObjectDeclaration<Pass>> packageObjectDeclarations = delcarationsByPackageAndClass.entrySet().stream()
                .filter(e -> !e.getKey().equals(moduleFqn))
                .map(e -> immutableEntry(
                        e.getKey(),
                        e.getValue().get(PackageObjectDeclaration.class).stream()
                                .map(d -> (PackageObjectDeclaration<Pass>) d)
                                .collect(single())))
                .collect(map());

        final ImmutableSetMultimap<Fqn, AbstractTypeDeclaration<Pass>> topLevelDeclarations = delcarationsByPackageAndClass.entrySet().stream()
                .flatMap(e -> e.getValue().get(AbstractTypeDeclaration.class).stream()
                        .map(d -> immutableEntry(e.getKey(), d)))
                .collect(setMultimap());

        final ImmutableMap<Fqn, Supplier<TemplateTypeConstructorMixin<Pass>>> dependencies = pass.getDependencyTypes().entrySet().stream()
                .map(d -> immutableEntry(d.getKey(), (Supplier<TemplateTypeConstructorMixin<Pass>>) d::getValue))
                .collect(map());

        final ImmutableSet<Fqn> moduleFqns = Stream.concat(Stream.of(moduleFqn), dependencies.keySet().stream()).collect(set());

        final ImmutableSet<Fqn> glueObjectFqns = moduleFqns.stream()
                .filter(fqn -> moduleFqns.stream().noneMatch(other -> other.strictlyContains(fqn)))
                .flatMap(fqn -> fqn.getParent().getAncestry().stream())
                .distinct()
                .collect(set());

        final ImmutableMap<Fqn, TemplateTypeConstructorMixin<Pass>> glueTypes = glueObjectFqns.stream()
                .map(fqn -> immutableEntry(fqn, AbstractTemplateTypeConstructor.<Pass>create(
                        TemplateTypeDeclaration.memoizing(
                                (x, b) -> TypeParameterListImpl.empty(),
                                (x, b) -> ImmutableList.of(),
                                (x, b) -> {
                                    final Stream<Map.Entry<String, TemplateTypeConstructorMixin<Pass>>> containedDependencies = x.getDependencyTypes().entrySet().stream()
                                            .filter(m -> m.getKey().getParent().equals(fqn))
                                            .map(m -> immutableEntry(m.getKey().getLocalName(), m.getValue()));

                                    final Stream<Map.Entry<String, TemplateTypeConstructorMixin<Pass>>> containedModules = Stream.concat(
                                            Optional.some(moduleFqn)
                                                    .filter(n -> n.getParent().equals(fqn))
                                                    .map(n -> immutableEntry(n.getLocalName(), x.lookUpTypeOf(moduleObjectDeclaration)))
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
                                                public SourceType<Pass> getType() {
                                                    return SourceType.of(e.getValue());
                                                }
                                            })
                                            .collect(set()));
                                },
                                (x, b) -> IndividualTags.of(TypeTags.NAME, FullyQualifiedTypeName.create(() -> fqn)))
                ).bind(pass)))
                .collect(map());

        return new ModuleGlueData(moduleObjectDeclaration, packageObjectDeclarations, topLevelDeclarations, glueTypes);
    }
}
