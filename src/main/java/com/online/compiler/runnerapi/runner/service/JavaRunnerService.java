package com.online.compiler.runnerapi.runner.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
@RequiredArgsConstructor
public class JavaRunnerService {

    private final FileWriter fileWriter;
    private final DockerService dockerService;

    public CompletableFuture<Integer> compileAndExecuteCode(String code) {
        return fileWriter.write(code)
                .thenApply(dockerService::startTerminal);
    }
}
