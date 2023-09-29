package br.com.rinha.compiladores;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void testAdd2() {
        run("/examples/add2.json", "2a");
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
    void testFibRec() {
        run("/examples/fibrec.json", "1836311903");
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
    
//    @Test
//    @SneakyThrows
//    void testFunctionPrint() {
//        run("/examples/function_print.json", "1\n2");
//    }
    
    
    
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
    
    
}
