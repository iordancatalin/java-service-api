package com.online.compiler.runnerapi.runner.executor.process;

import com.online.compiler.runnerapi.runner.executor.CodeExecutor;
import com.online.compiler.runnerapi.runner.model.ExecutionLog;
import com.online.compiler.runnerapi.runner.model.LogLevelEnum;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Service
public class ProcessCodeExecutor implements CodeExecutor {

    private final String javaRunnerPath;
    private final String javaRunnerJarName;
    private final String separator;

    public ProcessCodeExecutor(@Value("${java.runner.path}") String javaRunnerPath,
                               @Value("${java.runner.jar.name}") String javaRunnerJarName,
                               @Value("${java.runner.content.separator}") String separator) {
        this.javaRunnerPath = javaRunnerPath;
        this.javaRunnerJarName = javaRunnerJarName;
        this.separator = separator;
    }

    @Override
    public CompletableFuture<List<ExecutionLog>> executeCompiledCode(String pathToClassDirectory,
                                                                     String outputFilePath,
                                                                     String className) {
        try {
            final var command = buildRunCommand(pathToClassDirectory, outputFilePath, className);

            final var processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(javaRunnerPath));

            final var process = processBuilder.start();
            return process.onExit()
                    .thenApply(processFinished -> {
                        if (processFinished.exitValue() != 0) {
                            logErrorStream(processFinished.getErrorStream());
                        }
                        return getLogsFromOutputFile(outputFilePath);
                    });

        } catch (IOException e) {
            log.error(e);
        }

        return CompletableFuture.supplyAsync(List::of);
    }

    private List<ExecutionLog> getLogsFromOutputFile(String outputFilePath) {
        try (Stream<String> stream = Files.lines(Path.of(outputFilePath))) {
            return stream.map(this::buildExecutionLog)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error(e);
        }

        return List.of();
    }

    private void logErrorStream(InputStream errorStream) {
        try (var stream = new BufferedReader(new InputStreamReader(errorStream)).lines()) {
            stream.forEach(log::error);
        }
    }

    private ExecutionLog buildExecutionLog(String encodedLog) {
        if (encodedLog.isBlank()) {
            return new ExecutionLog("", LogLevelEnum.INFO);
        }

        final var content = encodedLog.split(separator);
        final var decodedMessageBytes = Base64.getDecoder().decode(content[0]);
        final var decodedMessage = new String(decodedMessageBytes);
        final var logLevel = LogLevelEnum.fromValue(content[1]).orElse(LogLevelEnum.INFO);

        return new ExecutionLog(decodedMessage, logLevel);
    }

    private List<String> buildRunCommand(String pathToClassDirectory, String outputFilePath, String className) {
        return List.of("java", "-jar", javaRunnerJarName, pathToClassDirectory, outputFilePath, className);
    }
}
