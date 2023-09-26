package br.com.rinha.compiladores;

import java.io.FileInputStream;

import br.com.rinha.compiladores.runtime.Runner;
import br.com.rinha.compiladores.translate.RinhaAstToJavaSource;

public class Compiler {

    public static void main(String[] args) throws Exception {
        
        var in = new FileInputStream(args[0]);
        var cu = RinhaAstToJavaSource.translate(in);
        //System.err.println("===========================================");
        //System.err.println(cu);
        Runner.run(cu);

    }
    


}
