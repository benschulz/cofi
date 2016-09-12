package de.benshu.cofi.cofic;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.cofic.backend.ToRuntimeModelTransformer;
import de.benshu.cofi.cofic.frontend.glue.ModuleGlueData;
import de.benshu.cofi.cofic.frontend.glue.ModuleGlueTyper;
import de.benshu.cofi.cofic.frontend.companions.CompanionObjectNormalizer;
import de.benshu.cofi.cofic.frontend.constraints.HierarchyAndConstraintEstablisher;
import de.benshu.cofi.cofic.frontend.discovery.Discoverer;
import de.benshu.cofi.cofic.frontend.implementations.ImplementationTyper;
import de.benshu.cofi.cofic.frontend.interfaces.InterfaceTyper;
import de.benshu.cofi.cofic.model.binary.BinaryDeserializer;
import de.benshu.cofi.cofic.model.binary.BinaryModule;
import de.benshu.cofi.cofic.model.common.TypeTags;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.interpreter.CofiInterpreter;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.cofi.parser.EarleyCofiParser;
import de.benshu.cofi.runtime.Module;
import de.benshu.cofi.runtime.context.RuntimeContext;
import de.benshu.cofi.runtime.serialization.ModuleDeserializer;
import de.benshu.cofi.runtime.serialization.ModuleSerializer;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Stream;

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
        interpret();

        return System.currentTimeMillis() - start;
    }

    private static void compile() throws IOException {
        final Path cofiLangPath = findRoot().resolve("target/.cofi.lang.cm");

        final BinaryModule cofiLang = Files.exists(cofiLangPath) && System.getProperty("clean") == null
                ? read(cofiLangPath, new BinaryDeserializer()::deserialize)
                : compile(Fqn.from("cofi", "lang"));

        compile(Fqn.from("helloworld"), ImmutableSet.of(cofiLang));
    }

    private static BinaryModule compile(Fqn moduleName) throws IOException {
        return compile(moduleName, ImmutableSet.of());
    }

    private static BinaryModule compile(Fqn moduleName, ImmutableSet<BinaryModule> dependencies) throws IOException {
        begin("Parsing…");
        ImmutableSet<CompilationUnit<Pass>> rawCompilationUnits = parseCompilationUnits(moduleName.toCanonicalString());
        end();

        Pass pass = createPass(moduleName, dependencies);

        begin("Normalizing meta objects…");
        final ImmutableSet<CompilationUnit<Pass>> normalizedCompilationUnints = CompanionObjectNormalizer.normalize(pass, rawCompilationUnits);
        end();

        begin("Creating glue types…");
        pass.setModuleGlueData(ModuleGlueTyper.type(pass, moduleName, normalizedCompilationUnints));
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

        return new BinaryDeserializer().deserialize(new StringReader(json));
    }

    private static Pass createPass(Fqn moduleFqn, ImmutableSet<BinaryModule> dependencies) {
        return new Pass(
                moduleFqn,
                dependencies,
                pass -> TypeSystemImpl.create(
                        name -> pass.resolveQualifiedTypeName(Fqn.from("cofi", "lang", name)),
                        TypeTags.NAME,
                        () -> (TemplateTypeConstructorMixin<Pass>) pass.resolveQualifiedTypeName(Fqn.from("cofi", "lang", "Object")))
        );
    }

    private static ImmutableSet<CompilationUnit<Pass>> parseCompilationUnits(String moduleName) {
        return findCompilationUnits(moduleName)
                .map(p -> read(p, EarleyCofiParser.INSTANCE::<Pass>parse))
                .collect(set());
    }

    private static <R> R read(Path path, Function<Reader, R> f) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return f.apply(reader);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static void interpret() throws IOException {
        begin("Loading module files…");
        RuntimeContext runtimeContext = new RuntimeContext();
        final Module cofiLang = read(findRoot().resolve("target/.cofi.lang.cm"), new ModuleDeserializer(runtimeContext, ImmutableSet.of())::deserialize);
        final Module helloworld = read(findRoot().resolve("target/.helloworld.cm"), new ModuleDeserializer(runtimeContext, ImmutableSet.of(cofiLang))::deserialize);
        runtimeContext.load(cofiLang);
        runtimeContext.load(helloworld);
        end();

        begin("Interpreting program…");
        System.out.println();
        new CofiInterpreter(runtimeContext).start(helloworld, "HelloWorld");
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
