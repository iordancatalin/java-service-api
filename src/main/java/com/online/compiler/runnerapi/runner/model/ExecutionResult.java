package com.online.compiler.runnerapi.runner.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExecutionResult {

    private final String output;
    private final String error;
}
