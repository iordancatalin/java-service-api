package com.online.compiler.runnerapi.features.runner.model;

import lombok.Data;

@Data
public class RunnerRequestModel {
    private String javaVersion;
    private String code;
}
