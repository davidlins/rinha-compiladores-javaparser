package br.com.rinha.compiladores.runtime;

import java.util.Collections;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import com.github.javaparser.ast.CompilationUnit;

public class Runner {

    
    public static void run(CompilationUnit cu) throws Exception {

        var className = cu.getType(0).getName().getIdentifier();
      
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        InMemoryFileManager manager = new InMemoryFileManager(compiler.getStandardFileManager(null, null, null));

        List<JavaFileObject> sourceFiles = Collections
                .singletonList(new JavaSourceFromString(className, cu.toString()));

        JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics, null, null, sourceFiles);

        boolean result = task.call();

        if (!result) {
            diagnostics.getDiagnostics()
                    .forEach(d -> System.out.println(String.valueOf(d)));
        } else {
            ClassLoader classLoader = manager.getClassLoader(null);
            Class<?> clazz = classLoader.loadClass(className);
            InMemoryClass instanceOfClass = (InMemoryClass) clazz.getDeclaredConstructor().newInstance();
            instanceOfClass.runCode();
        }
    }
}
