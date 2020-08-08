package com.online.compiler.runnerapi.runner.executor.process;

import com.online.compiler.runnerapi.runner.executor.CodeExecutor;
import com.online.compiler.runnerapi.runner.model.ExecutionLog;
import com.online.compiler.runnerapi.runner.model.LogLevelEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProcessCodeExecutor implements CodeExecutor {

    private static final String JAVA_RUNNER_PATH = "C:/projects/disertatie/jars";
    private static final String JAVA_RUNNER_JAR_NAME = "java-runner-1.0-SNAPSHOT.jar";
    private static final String SEPARATOR = "\\.";

    @Override
    public List<ExecutionLog> executeCompiledCode(String pathToClassDirectory,
                                                  String outputFilePath,
                                                  String className) {
        try {
            final var command = buildRunCommand(pathToClassDirectory, outputFilePath, className);

            final var processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(JAVA_RUNNER_PATH));

            final var process = processBuilder.start();
            process.waitFor();

            try (Stream<String> stream = Files.lines(Path.of(outputFilePath))) {
                return stream.map(this::buildExecutionLog)
                        .collect(Collectors.toList());
            }
        } catch (IOException | InterruptedException e) {
            log.error(e);
        }

        return List.of();
    }

    private ExecutionLog buildExecutionLog(String encodedLog) {
        if (encodedLog.isBlank()) {
            return new ExecutionLog("", LogLevelEnum.INFO);
        }

        final var content = encodedLog.split(SEPARATOR);
        final var decodedMessageBytes = Base64.getDecoder().decode(content[0]);
        final var decodedMessage = new String(decodedMessageBytes);
        final var logLevel = LogLevelEnum.fromValue(content[1]).orElse(LogLevelEnum.INFO);

        return new ExecutionLog(decodedMessage, logLevel);
    }

    private List<String> buildRunCommand(String pathToClassDirectory, String outputFilePath, String className) {
        return List.of("java", "-jar", JAVA_RUNNER_JAR_NAME, pathToClassDirectory, outputFilePath, className);
    }
}
