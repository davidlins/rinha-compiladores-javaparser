package br.com.rinha.compiladores;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;

import br.com.rinha.compiladores.runtime.Runner;
import br.com.rinha.compiladores.translate.RinhaAstToJavaSource;
import lombok.SneakyThrows;

class RinhaAstToJavaSourceTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    @SneakyThrows
    void testHello() {
        run("/examples/hello.json", "Hello, world!");
    }

    @Test
    @SneakyThrows
    void testAdd() {
        run("/examples/add.json", "1 + 2 = 12");
    }

    @Test
    @SneakyThrows
    void testLet() {
        run("/examples/let.json", "3");
    }

    @Test
    @SneakyThrows
    void testSum() {
        run("/examples/sum.json", "15");
    }

    @Test
    @SneakyThrows
    void testFib() {
        run("/examples/fib.json", "fib: 55");
    }

    @Test
    @SneakyThrows
    void testCombination() {
        run("/examples/combination.json", "45");
    }

    @SneakyThrows
    void run(String astJsonPath, String expect) {

        var in = RinhaAstToJavaSourceTest.class.getResourceAsStream(astJsonPath);
        var cu = RinhaAstToJavaSource.translate(in);
        System.err.println("===========================================");
        System.err.println(cu);
        Runner.run(cu);

        var consoleString = outputStreamCaptor.toString()
                .trim();

        System.err.println(">>>>> " + consoleString);
        System.err.println("===========================================");
        assertEquals(expect, consoleString);

    }

    @Test
    @SneakyThrows
    void testInspec() {

        var tuple = "public class Tuple{\n"
                + "    \n"
                + "    Object first ;\n"
                + "    Object second;\n"
                + "\n"
                + "    public Object getFirst() {\n"
                + "        return first;\n"
                + "    }\n"
                + "    public Object getSecond() {\n"
                + "        return second;\n"
                + "    }\n"
                + "public static class Tuple1{\n"
                + "    \n"
                + "    Object first ;\n"
                + "    Object second;\n"
                + "\n"
                + "    public Object getFirst() {\n"
                + "        return first;\n"
                + "    }\n"
                + "    public Object getSecond() {\n"
                + "        return second;\n"
                + "    }\n"
                + "}\n"

                + "}";

        var statement = StaticJavaParser.parse(tuple);
        System.err.println(statement);
    }
}
