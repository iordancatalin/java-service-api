package com.online.compiler.runnerapi.runner.executor;

import com.online.compiler.runnerapi.runner.model.ExecutionLog;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CodeExecutor {
    CompletableFuture<List<ExecutionLog>> executeCompiledCode(String pathToClassDirectory,
                                                              String outputFilePath,
                                                              String className);
}
