package com.online.compiler.runnerapi.runner;

import com.online.compiler.runnerapi.runner.compiler.Compiler;
import com.online.compiler.runnerapi.runner.compiler.FileWriter;
import com.online.compiler.runnerapi.runner.exception.CodeNotCompilableException;
import com.online.compiler.runnerapi.runner.executor.CodeExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.online.compiler.runnerapi.common.Constants.*;

@Service
@RequiredArgsConstructor
public class JavaRunnerService {

    @Qualifier("processCodeExecutor")
    private final CodeExecutor codeExecutor;

    private final FileWriter fileWriter;
    private final Compiler compiler;

    public CompletableFuture<String> compileAndExecuteCode(String code) {
        return fileWriter.write(code)
                .thenApply(this::compileAndValidateCode)
                .thenApply(this::executeCompiledCode)
                .orTimeout(1, TimeUnit.MINUTES);
    }

    private String compileAndValidateCode(String classDirectoryName) {
        final var pathToJavaFile = createJavaFilePath(classDirectoryName);
        final var diagnosticCollector = compiler.compile(pathToJavaFile);

        if (!diagnosticCollector.getDiagnostics().isEmpty()) {
            throw new CodeNotCompilableException(diagnosticCollector);
        }

        return classDirectoryName;
    }

    private String executeCompiledCode(String classDirectoryName) {
        return codeExecutor.executeCompiledCode(classDirectoryName, FILE_NAME);
    }

    private Path createJavaFilePath(String parentDirectoryName) {
        final var path = COMPILED_CLASSES_DIRECTORY + "/" + parentDirectoryName + "/" + FILE_NAME_WITH_JAVA_EXTENSION;
        return Path.of(path);
    }
}
