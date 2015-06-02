package de.benshu.cofi.cofic;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.cofic.backend.ToRuntimeModelTransformer;
import de.benshu.cofi.cofic.frontend.ModuleGlueTyper;
import de.benshu.cofi.cofic.frontend.companions.CompanionObjectNormalizer;
import de.benshu.cofi.cofic.frontend.constraints.HierarchyAndConstraintEstablisher;
import de.benshu.cofi.cofic.frontend.discovery.Discoverer;
import de.benshu.cofi.cofic.frontend.implementations.ImplementationTyper;
import de.benshu.cofi.cofic.frontend.interfaces.InterfaceTyper;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.interpreter.CofiInterpreter;
import de.benshu.cofi.model.Module.Version;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.cofi.model.impl.ModuleImpl;
import de.benshu.cofi.parser.EarleyCofiParser;
import de.benshu.cofi.runtime.Module;
import de.benshu.cofi.runtime.context.RuntimeContext;
import de.benshu.cofi.runtime.serialization.ModuleDeserializer;
import de.benshu.cofi.runtime.serialization.ModuleSerializer;
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
        // TODO A separate hello-world module.

        begin("Parsing…");
        ImmutableSet<CompilationUnit<Pass>> rawCompilationUnits = findCompilationUnits()
                .map(p -> {
                    try {
                        return Files.newBufferedReader(p, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        throw Throwables.propagate(e);
                    }
                })
                .map(EarleyCofiParser.INSTANCE::<Pass>parse)
                .collect(set());
        end();

        Pass pass = new Pass();

        final ModuleImpl<Pass> langModule = ModuleImpl.create(pass, Fqn.from("cofi", "lang"), new Version());

        begin("Normalizing meta objects…");
        final ImmutableSet<CompilationUnit<Pass>> normalizedCompilationUnints = CompanionObjectNormalizer.normalize(pass, rawCompilationUnits);
        end();

        begin("Creating glue types…");
        ModuleGlueTyper.type(pass, langModule, ImmutableSet.of(), normalizedCompilationUnints);
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
        Files.write(findRoot().resolve("target").resolve("core.cm"), json.getBytes());
        end();
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

    private static Stream<Path> findCompilationUnits() {
        return findCompilationUnits(findRoot().resolve("cofi-src"));
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
            return cwd.resolve("cofi").resolve("cofic-testing");
        else if (Files.isDirectory(cwd.resolve("cofic-testing")))
            return cwd.resolve("cofic-testing");
        else if (Files.isDirectory(cwd.resolve("cofi-src")))
            return cwd;
        else
            throw new AssertionError();
    }
}
