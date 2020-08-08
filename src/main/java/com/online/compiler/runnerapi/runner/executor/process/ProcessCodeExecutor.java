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

@Log4j2
@Service
@RequiredArgsConstructor
public class ProcessCodeExecutor implements CodeExecutor {

    private static final String JAVA_RUNNER_PATH = "C:/projects/disertatie/jars";
    private static final String JAVA_RUNNER_JAR_NAME = "java-runner-1.0-SNAPSHOT.jar";

    @Override
    public String executeCompiledCode(String pathToClassDirectory, String outputFilePath, String className) {
        try {
            final var command = buildRunCommand(pathToClassDirectory, outputFilePath, className);

            final var processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(JAVA_RUNNER_PATH));

            final var process = processBuilder.start();
            process.waitFor();

            try (Stream<String> stream = Files.lines(Path.of(outputFilePath))) {
                return stream.collect(Collectors.joining("\n"));
            }
        } catch (IOException | InterruptedException e) {
            log.error(e);
        }

        return "";
    }

    private List<String> buildRunCommand(String pathToClassDirectory, String outputFilePath, String className) {
        return List.of("java", "-jar", JAVA_RUNNER_JAR_NAME, pathToClassDirectory, outputFilePath, className);
    }
}
