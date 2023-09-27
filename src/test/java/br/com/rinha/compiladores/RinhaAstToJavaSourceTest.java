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
    
    @Test
    @SneakyThrows
    void testTupleFirst() {
        run("/examples/tupla_first.json", "1");
    }
    
    @Test
    @SneakyThrows
    void testTupleSecond() {
        run("/examples/tupla_second.json", "2");
    }
    
    @Test
    @SneakyThrows
    void testPrint1() {
        run("/examples/print1.json", "1\n2\n3");
    }
    
    @Test
    @SneakyThrows
    void testPrintTuple() {
        run("/examples/print_tuple.json", "1\n2\n(1, 2)");
    }

    @Test
    @SneakyThrows
    void testLetPrint() {
        run("/examples/let_print.json", "1\n2");
    }
    
    @Test
    @SneakyThrows
    void testFunctionPrint() {
        run("/examples/function_print.json", "1\n2");
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

        var tuple = "public class MainClass {\n"
                + "\n"
                + "    public static Object first(Tuple tuple) {\n"
                + "        return tuple.getFirst();\n"
                + "    }\n"
                + "    \n"
                + "    public static Object secund(Tuple tuple) {\n"
                + "        return tuple.getSecond();\n"
                + "    }\n"
                + "    \n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(first(new Tuple(1,2)));\n"
                + "    }\n"
                + "\n"
                + "    public static class Tuple {\n"
                + "\n"
                + "        Object first;\n"
                + "        Object second;\n"
                + "\n"
                + "        \n"
                + "        public Tuple(Object first, Object second) {\n"
                + "            super();\n"
                + "            this.first = first;\n"
                + "            this.second = second;\n"
                + "        }\n"
                + "        \n"
                + "\n"
                + "        public Object getFirst() {\n"
                + "            return first;\n"
                + "        }\n"
                + "\n"
                + "        public Object getSecond() {\n"
                + "            return second;\n"
                + "        }\n"
                + "        \n"
                + "        public String toString() {\n"
                + "            return \"(\"+first+\",\"+second+\")\";\n"
                + "        }\n"
                + "    }\n"
                + "}";
        
        var statement = StaticJavaParser.parse(tuple);
        System.err.println(statement);
    }
}
