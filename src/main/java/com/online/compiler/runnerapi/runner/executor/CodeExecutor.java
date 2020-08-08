package com.online.compiler.runnerapi.runner.executor;

import com.online.compiler.runnerapi.runner.model.ExecutionLog;

import java.util.List;

public interface CodeExecutor {
    List<ExecutionLog> executeCompiledCode(String pathToClassDirectory,
                                           String outputFilePath,
                                           String className);
}
