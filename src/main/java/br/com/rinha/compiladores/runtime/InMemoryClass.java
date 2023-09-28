package br.com.rinha.compiladores.runtime;

public interface InMemoryClass {

    static Object print(Object value) {
        System.out.print(value);
        System.out.println();
        return value;
    }

    static int print(int value) {
        System.out.print(value);
        System.out.println();
        return value;
    }

    void runCode();
}