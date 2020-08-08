package com.online.compiler.runnerapi.runner;

import com.online.compiler.runnerapi.runner.compiler.Compiler;
import com.online.compiler.runnerapi.runner.compiler.FileWriter;
import com.online.compiler.runnerapi.runner.exception.CodeNotCompilableException;
import com.online.compiler.runnerapi.runner.executor.CodeExecutor;
import com.online.compiler.runnerapi.runner.model.ExecutionLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.online.compiler.runnerapi.common.Constants.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class JavaRunnerService {

    @Qualifier("processCodeExecutor")
    private final CodeExecutor codeExecutor;

    private final FileWriter fileWriter;
    private final Compiler compiler;

    public CompletableFuture<List<ExecutionLog>> compileAndExecuteCode(String code) {
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

    private List<ExecutionLog> executeCompiledCode(String classDirectoryName) {
        final var pathToClassDirectory = COMPILED_CLASSES_DIRECTORY + "/" + classDirectoryName;
        final var outputFilePath = pathToClassDirectory + "/" + OUTPUT_FILE_NAME;

        createOutputFile(outputFilePath);

        return codeExecutor.executeCompiledCode(pathToClassDirectory, outputFilePath, FILE_NAME);
    }

    private void createOutputFile(String outputFilePath) {
        final var file = new File(outputFilePath);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    private Path createJavaFilePath(String parentDirectoryName) {
        final var path = COMPILED_CLASSES_DIRECTORY + "/" + parentDirectoryName + "/" + FILE_NAME_WITH_JAVA_EXTENSION;
        return Path.of(path);
    }
}
