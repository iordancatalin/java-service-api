package com.online.compiler.runnerapi.runner.model;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExecutionLog {

    private final String message;
    private final LogLevelEnum level;
}
