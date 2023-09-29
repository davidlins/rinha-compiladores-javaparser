
package br.com.rinha.compiladores.runtime;

public interface Binary {

    static Object handleBinary(Object lhs, String op, Object rhs) {
       
        switch (op.toUpperCase()) {
        case "ADD":
            return (lhs instanceof String || rhs instanceof String) ? lhs.toString() + rhs.toString() : (Integer) lhs + (Integer) rhs ;
        case "SUB":
            return (Integer) lhs - (Integer) rhs ;
        case "MUL":
            return (Integer) lhs * (Integer) rhs ;
        case "DIV":
            return (Integer) lhs / (Integer) rhs ;
        case "REM":
            return (Integer) lhs % (Integer) rhs ;
        case "LT":
            return (Integer) lhs < (Integer) rhs ;
        case "GT":
            return (Integer) lhs > (Integer) rhs ;
        case "LTE":
            return (Integer) lhs <= (Integer) rhs ;
        case "GTE":
            return (Integer) lhs >= (Integer) rhs ;
        case "EQ":
            return lhs.equals(rhs) ;
        case "NEQ":
            return !lhs.equals(rhs) ;
        case "AND":
            return (Boolean) lhs && (Boolean) rhs ;
        case "OR":
            return (Boolean) lhs || (Boolean) rhs ;    
        default:
            throw new IllegalArgumentException("Unexpected handleBinary: " + op);
            
        }

    }

}
