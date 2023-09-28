package br.com.rinha.compiladores.runtime;

public class Tuple {

    Object first;
    Object second;

    public Tuple(Object first, Object second) {
        super();
        this.first = first;
        this.second = second;
    }

    public Object getFirst() {
        return first;
    }

    public Object getSecond() {
        return second;
    }

    public String toString() {
        return "(" + first + ", " + second + ")";
    }
    
    public static Object first(Tuple tuple) {
        return tuple.getFirst();
    }

    public static Object second(Tuple tuple) {
        return tuple.getSecond();
    }
    
}
