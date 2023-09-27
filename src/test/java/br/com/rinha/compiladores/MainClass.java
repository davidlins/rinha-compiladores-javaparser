package br.com.rinha.compiladores;

public class MainClass {

    public static Object first(Tuple tuple) {
        return tuple.getFirst();
    }
    
    public static Object secund(Tuple tuple) {
        return tuple.getSecond();
    }
    
    public static void main(String[] args) {
        System.out.println(first(new Tuple(1,2)));
    }

    public static class Tuple {

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
            return "("+first+", "+second+")";
        }
    }
}