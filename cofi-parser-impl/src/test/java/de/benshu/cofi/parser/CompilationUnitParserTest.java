package de.benshu.cofi.parser;

import de.benshu.cofi.parser.lexer.UnexpectedTokenException;
import org.testng.annotations.Test;

import java.io.StringReader;

public class CompilationUnitParserTest {
    @Test
    public void helloWorld() throws UnexpectedTokenException {
        final Object result = EarleyCofiParser.INSTANCE.parse(new StringReader("" +
                "module .hello.world;\n" +
                "package .hello.world;\n" +
                "\n" +
                "public object HelloWorld extends Application {\n" +
                "    public start(args : ImmutableList<String>) : () {\n" +
                "        Console.println(\"Hello World!\");" +
                "    }\n" +
                "}\n"));

        System.out.println(result);
    }
}
