package com.online.compiler.runnerapi.runner.executor;

public interface CodeExecutor {
    String executeCompiledCode(String pathToClassDirectory, String outputFilePath, String className);
}
