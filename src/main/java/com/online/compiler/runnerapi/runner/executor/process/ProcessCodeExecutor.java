package com.online.compiler.runnerapi.runner.executor.process;

import com.online.compiler.runnerapi.runner.executor.CodeExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.online.compiler.runnerapi.common.Constants.COMPILED_CLASSES_DIRECTORY;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProcessCodeExecutor implements CodeExecutor {

    private static final String JAVA_RUNNER_PATH = "C:/projects/disertatie/jars";
    private static final String JAVA_RUNNER_JAR_NAME = "java-runner-1.0-SNAPSHOT.jar";
    private static final String OUTPUT_FILE_NAME = "output.txt";

    @Override
    public String executeCompiledCode(String classDirectoryName, String className) {
        try {
            final var pathToClassDirectory = toClassDirectory(classDirectoryName);
            final var pathToClassDirectoryAbsolutePath = pathToClassDirectory.toAbsolutePath().toString();

            final var command = buildRunCommand(pathToClassDirectoryAbsolutePath, className);

            final var processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(JAVA_RUNNER_PATH));

            final var process = processBuilder.start();
            process.waitFor();

            try (Stream<String> stream = Files.lines(toOutputPath(pathToClassDirectoryAbsolutePath))) {
                return stream.collect(Collectors.joining("\n"));
            }
        } catch (IOException | InterruptedException e) {
            log.error(e);
        }

        return null;
    }

    private List<String> buildRunCommand(String pathToClassDirectory, String className) {
        return List.of("java", "-jar", JAVA_RUNNER_JAR_NAME, pathToClassDirectory, className);
    }

    private Path toOutputPath(String pathToClassDirectoryAbsolutePath) {
        final var path = pathToClassDirectoryAbsolutePath + "/" + OUTPUT_FILE_NAME;
        return Path.of(path);
    }

    private Path toClassDirectory(String classDirectoryName) {
        final var path = COMPILED_CLASSES_DIRECTORY + "/" + classDirectoryName;
        return Path.of(path);
    }
}
