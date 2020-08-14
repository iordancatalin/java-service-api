package com.online.compiler.runnerapi.features.runner.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BuildArgModel {

    private final String argName;
    private final String argValue;
}
