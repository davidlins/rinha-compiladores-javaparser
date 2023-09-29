package br.com.rinha.compiladores;

import br.com.rinha.compiladores.runtime.InMemoryClass;
import br.com.rinha.compiladores.runtime.Tuple;
import static br.com.rinha.compiladores.runtime.Tuple.*;
import static br.com.rinha.compiladores.runtime.Binary.*;
import static br.com.rinha.compiladores.runtime.InMemoryClass.*;

public class Fibrec1 {

    public static void main(String ...args) {
        print(fib(100000));
    }

    public static Object fibrec(Object wn, Object wk1, Object wk2) {
        var n = wn;
        var k1 = wk1;
        var k2 = wk2;
        if ((Boolean) handleBinary(n, "Eq", 0)) {
            return k1;
        } else {
            if ((Boolean) handleBinary(n, "Eq", 1)) {
                return k2;
            } else {
                return fibrec(handleBinary(n, "Sub", 1), k2, handleBinary(k1, "Add", k2));
            }
        }
    }

    public static Object fib(Object wn) {
        var n = wn;
        return fibrec(n, 0, 1);
    }
}
