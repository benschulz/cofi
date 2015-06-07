package de.benshu.cofi.cofic;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.cofic.backend.ToRuntimeModelTransformer;
import de.benshu.cofi.cofic.frontend.ModuleGlueTyper;
import de.benshu.cofi.cofic.frontend.companions.CompanionObjectNormalizer;
import de.benshu.cofi.cofic.frontend.constraints.HierarchyAndConstraintEstablisher;
import de.benshu.cofi.cofic.frontend.discovery.Discoverer;
import de.benshu.cofi.cofic.frontend.implementations.ImplementationTyper;
import de.benshu.cofi.cofic.frontend.interfaces.InterfaceTyper;
import de.benshu.cofi.cofic.model.binary.BinaryDeserializer;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.interpreter.CofiInterpreter;
import de.benshu.cofi.model.Module.Version;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.cofi.model.impl.ModuleImpl;
import de.benshu.cofi.model.impl.ObjectDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.cofi.cofic.model.common.TypeTags;
import de.benshu.cofi.parser.EarleyCofiParser;
import de.benshu.cofi.runtime.Module;
import de.benshu.cofi.runtime.context.RuntimeContext;
import de.benshu.cofi.runtime.serialization.ModuleDeserializer;
import de.benshu.cofi.runtime.serialization.ModuleSerializer;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import de.benshu.commons.core.Optional;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.commons.core.streams.Collectors.set;

public class Testing {
    public static void main(String[] args) throws IOException {
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getRootLogger()
                .addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN)));

        warmup(0);

        final long time = run();
        System.out.println();
        System.out.println(" Done (" + (time / 1000f) + "s)");
    }

    private static void warmup(final int warmup) throws IOException {
        for (int i = 0; i < warmup; ++i) {
            run();
        }

        if (warmup > 0)
            System.out.println("════════════════════════════════════════");
    }

    private static long run() throws IOException {
        final long start = System.currentTimeMillis();

        compile();
        //interpret();

        return System.currentTimeMillis() - start;
    }

    private static void compile() throws IOException {
        Function<ImmutableList<String>, Optional<TypeMixin<Pass, ?>>> tryLookUpLangType;
        ImmutableMap<Fqn, TemplateTypeConstructorMixin<Pass>> moduleTypes;

        compile(Fqn.from("cofi", "lang"));
        compile(Fqn.from("helloworld"));
    }

    private static void compile(Fqn moduleName) throws IOException {
        begin("Parsing…");
        ImmutableSet<CompilationUnit<Pass>> rawCompilationUnits = parseCompilationUnits(moduleName.toCanonicalString());
        end();

        Pass pass = createPass();

        final ModuleImpl<Pass> module = ModuleImpl.create(pass, moduleName, new Version());

        begin("Normalizing meta objects…");
        final ImmutableSet<CompilationUnit<Pass>> normalizedCompilationUnints = CompanionObjectNormalizer.normalize(pass, rawCompilationUnits);
        end();

        begin("Creating glue types…");
        ModuleGlueTyper.type(pass, module, ImmutableSet.of(), normalizedCompilationUnints);
        end();

        begin("Discovering types…");
        pass.setDiscoveryData(Discoverer.discover(pass, normalizedCompilationUnints));
        end();

        begin("Establishing constraints…");
        pass.setConstraintsData(HierarchyAndConstraintEstablisher.establish(pass, normalizedCompilationUnints));
        end();

        begin("Typing interfaces…");
        pass.setInterfaceData(InterfaceTyper.type(pass, normalizedCompilationUnints));
        end();

        begin("Typing implementations…");
        pass.setImplementationData(ImplementationTyper.type(pass, normalizedCompilationUnints));
        end();

        begin("Constructing runtime model…");
        final Module langRtModule = ToRuntimeModelTransformer.transformModule(pass, normalizedCompilationUnints);
        end();

        begin("Writing module file…");
        final StringWriter output = new StringWriter();
        new ModuleSerializer().serialize(langRtModule, output);
        final String json = output.toString();
        Files.write(findRoot().resolve("target").resolve(moduleName.toCanonicalString() + ".cm"), json.getBytes());
        end();

        new BinaryDeserializer().deserialize(new StringReader(json));
    }

    private static Pass createPass() {
        final AtomicReference<Pass> hack = new AtomicReference<>();

        Function<ImmutableList<String>, Optional<TypeMixin<Pass, ?>>> tryLookUpLangType = names -> tryLookUpLangTypeInternal(hack.get(), names);

        Function<String, Optional<AbstractMember<Pass>>> tryLookUpLangMember = name -> tryLookUpLangMemberInternal(hack.get(), name);

        final TypeSystemImpl<Pass> typeSystem = TypeSystemImpl.create(
                name -> tryLookUpLangType.apply(ImmutableList.of(name)).get(),
                TypeTags.NAME,
                () -> (TemplateTypeConstructorMixin<Pass>) tryLookUpLangType.apply(ImmutableList.of("Object")).get());

        hack.set(new Pass(typeSystem, tryLookUpLangType, tryLookUpLangMember));
        return hack.get();
    }

    private static Optional<TypeMixin<Pass, ?>> tryLookUpLangTypeInternal(Pass pass, ImmutableList<String> names) {
        Fqn fqn = Fqn.from("cofi", "lang").getDescendant(names);

        Map.Entry<PackageObjectDeclaration<Pass>, ImmutableList<String>> pkgAndRemaining = fqn.getAncestry().stream()
                .sorted(Comparator.<Fqn>naturalOrder().reversed())
                .map(c -> pass.tryLookUpPackageObjectDeclarationOf(c).map(p -> immutableEntry(p, c.getRelativeNameOf(fqn))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst().get();

        PackageObjectDeclaration<Pass> pkg = pkgAndRemaining.getKey();
        ImmutableList<String> remaining = pkgAndRemaining.getValue();

        if (remaining.isEmpty())
            return Optional.some(pass.lookUpTypeOf(pkg));

        final ImmutableSet<AbstractTypeDeclaration<Pass>> tlds = pass.lookUpTopLevelDeclarationIn(pkg);

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

        return Optional.from(result
                .map(d -> pass.tryLookUpAccompaniedBy(d).getOrReturn(d))
                .map(pass::lookUpTypeOf));
    }

    private static Optional<AbstractMember<Pass>> tryLookUpLangMemberInternal(Pass pass, String name) {
        return pass.tryLookUpPackageObjectDeclarationOf(Fqn.from("cofi", "lang"))
                .map(pass::lookUpTypeOf)
                .map(t -> t.applyTrivially().lookupMember(name))
                .get();
    }

    private static ImmutableSet<CompilationUnit<Pass>> parseCompilationUnits(String moduleName) {
        return findCompilationUnits(moduleName)
                .map(p -> {
                    try {
                        return Files.newBufferedReader(p, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        throw Throwables.propagate(e);
                    }
                })
                .map(EarleyCofiParser.INSTANCE::<Pass>parse)
                .collect(set());
    }

    private static void interpret() throws IOException {
        begin("Loading module file…");
        String json = new String(Files.readAllBytes(findRoot().resolve("target").resolve("core.cm")), StandardCharsets.UTF_8);
        final RuntimeContext runtimeContext = new ModuleDeserializer().deserialize(new StringReader(json));
        Module deserializedLangModule = runtimeContext.getModule();
        end();

        begin("Interpreting program…");
        System.out.println();
        new CofiInterpreter(runtimeContext).start(deserializedLangModule);
        System.out.print(" … ...........................");
        end();
    }

    private static long taskBegin;

    private static void begin(String task) {
        System.out.print(" " + task + " " + "............................".substring(task.length()));
        taskBegin = System.currentTimeMillis();
    }

    private static void end() {
        final long time = System.currentTimeMillis() - taskBegin;
        System.out.println(" [" + (time / 1000f) + "s]");
    }

    private static Stream<Path> findCompilationUnits(String moduleName) {
        return findCompilationUnits(findRoot().resolve("cofi-src").resolve(moduleName));
    }

    private static Stream<Path> findCompilationUnits(Path sourceDirectory) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.cofi");

        try {
            return Files.list(sourceDirectory)
                    .flatMap(p -> Files.isDirectory(p)
                            ? findCompilationUnits(p)
                            : Stream.of(p))
                    .filter(matcher::matches);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static Path findRoot() {
        final Path cwd = Paths.get(".");

        if (Files.isDirectory(cwd.resolve("cofi")))
            return cwd.resolve("cofi/cofic-testing");
        else if (Files.isDirectory(cwd.resolve("cofic-testing")))
            return cwd.resolve("cofic-testing");
        else if (Files.isDirectory(cwd.resolve("cofi-src")))
            return cwd;
        else
            throw new AssertionError();
    }
}
