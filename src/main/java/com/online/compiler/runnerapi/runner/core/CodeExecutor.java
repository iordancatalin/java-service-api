package com.online.compiler.runnerapi.runner.core;

import com.online.compiler.runnerapi.common.Constants;
import com.online.compiler.runnerapi.runner.model.ExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.online.compiler.runnerapi.common.Constants.COMPILED_CLASSES_DIRECTORY;
import static com.online.compiler.runnerapi.common.Constants.FILE_NAME_WITH_JAVA_EXTENSION;

@Log4j2
@Service
@RequiredArgsConstructor
public class CodeExecutor {

    private static final String JAVA_RUNNER_PATH = "C:/projects/disertatie/jars";
    private static final String JAVA_RUNNER_JAR_NAME = "java-runner-1.0-SNAPSHOT.jar";
    private static final String OUTPUT_FILE_NAME = "output.txt";

    private final FileWriter fileWriter;
    private final Compiler compiler;

    public CompletableFuture<String> executeCode(String code) {
        return fileWriter.write(code)
                .thenApply(this::compileAndExecuteCode);
    }

    private String compileAndExecuteCode(String parentDirectoryName) {
        compileAndValidateCode(parentDirectoryName);
        return executeCompiledCode(parentDirectoryName);
    }

    private String executeCompiledCode(String parentDirectoryName) {
        try {
            final var parentDirectoryAbsolutePath = toParentDirectory(parentDirectoryName).toAbsolutePath();
            final var command = buildRunCommand(parentDirectoryAbsolutePath.toString());

            final var processBuilder = new ProcessBuilder(command);
            final var file = new File(JAVA_RUNNER_PATH);

            processBuilder.directory(file);

            final var process = processBuilder.start();
            process.waitFor();

            try (Stream<String> stream = Files.lines(toOutputPath(parentDirectoryAbsolutePath))) {
                return stream.collect(Collectors.joining("\n"));
            }
        } catch (IOException | InterruptedException e) {
            log.error(e);
        }

        return null;
    }

    private List<String> buildRunCommand(String pathToClassDirectory) {
        return List.of("java", "-jar", JAVA_RUNNER_JAR_NAME, pathToClassDirectory, Constants.FILE_NAME);
    }

    private void compileAndValidateCode(String parentDirectoryName) {
        final var pathToClass = createJavaFilePath(parentDirectoryName);
        final var diagnosticCollector = compiler.compile(pathToClass);

        if (!diagnosticCollector.getDiagnostics().isEmpty()) {
            throw new RuntimeException("Cannot compile java code");
        }
    }

    private Path toOutputPath(Path parentDirectoryAbsolutePath) {
        final var path = parentDirectoryAbsolutePath.toString() + "/" + OUTPUT_FILE_NAME;
        return Path.of(path);
    }

    private Path toParentDirectory(String parentDirectoryName) {
        final var path = COMPILED_CLASSES_DIRECTORY + "/" + parentDirectoryName;
        return Path.of(path);
    }

    private Path createJavaFilePath(String parentDirectoryName) {
        final var path = COMPILED_CLASSES_DIRECTORY + "/" + parentDirectoryName + "/" + FILE_NAME_WITH_JAVA_EXTENSION;
        return Path.of(path);
    }
}
