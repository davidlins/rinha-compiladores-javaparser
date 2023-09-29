package br.com.rinha.compiladores;

import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;

import lombok.SneakyThrows;

class InspecTest {

   

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
        
//        var statement = StaticJavaParser.parse(tuple);
//        var statement = StaticJavaParser.parseExpression("(Boolean) handleBinary(n, \"GT\",2) && (Boolean) handleBinary(n, \"Lt\",10)");
//        System.err.println("1 + 2 = " + 1 + 2);
        
        var statement = StaticJavaParser.parseExpression("(int a, int b) -> { return a + b; }");
        System.err.println(statement);
        
        var a = 10;
        //System.out.println(() -> {a - 1});
        
    }
    
    
    
}
