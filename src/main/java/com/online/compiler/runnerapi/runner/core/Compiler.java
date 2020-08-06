package com.online.compiler.runnerapi.runner.core;

import org.springframework.stereotype.Service;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class Compiler {

    public DiagnosticCollector<JavaFileObject> compile(Path path) {
        final var diagnostics = new DiagnosticCollector<JavaFileObject>();
        final var compiler = ToolProvider.getSystemJavaCompiler();

        try (var fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {

            Iterable<? extends JavaFileObject> compilationUnit =
                    fileManager.getJavaFileObjectsFromPaths(List.of(path));

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    List.of(),
                    null,
                    compilationUnit);

            task.call();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return diagnostics;
    }

}