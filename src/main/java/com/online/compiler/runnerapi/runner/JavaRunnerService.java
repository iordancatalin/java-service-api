package com.online.compiler.runnerapi.runner;

import com.online.compiler.runnerapi.runner.compiler.Compiler;
import com.online.compiler.runnerapi.runner.compiler.FileWriter;
import com.online.compiler.runnerapi.runner.docker.DockerService;
import com.online.compiler.runnerapi.runner.exception.CodeNotCompilableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static com.online.compiler.runnerapi.common.Constants.COMPILED_CLASSES_DIRECTORY;
import static com.online.compiler.runnerapi.common.Constants.FILE_NAME_WITH_JAVA_EXTENSION;

@Log4j2
@Service
@RequiredArgsConstructor
public class JavaRunnerService {

    private final FileWriter fileWriter;
    private final Compiler compiler;
    private final DockerService dockerService;

    public CompletableFuture<Integer> compileAndExecuteCode(String code) {
        return fileWriter.write(code)
                .thenApply(this::compileAndValidateCode)
                .thenApply(dockerService::startTerminal);
    }

    private String compileAndValidateCode(String classDirectoryName) {
        final var pathToJavaFile = createJavaFilePath(classDirectoryName);
        final var diagnosticCollector = compiler.compile(pathToJavaFile);

        if (!diagnosticCollector.getDiagnostics().isEmpty()) {
            throw new CodeNotCompilableException(diagnosticCollector);
        }

        return classDirectoryName;
    }

    private Path createJavaFilePath(String parentDirectoryName) {
        final var path = COMPILED_CLASSES_DIRECTORY + "/" + parentDirectoryName + "/" + FILE_NAME_WITH_JAVA_EXTENSION;
        return Path.of(path);
    }
}
