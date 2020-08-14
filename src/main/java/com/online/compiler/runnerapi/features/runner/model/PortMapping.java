package com.online.compiler.runnerapi.features.runner.model;

import com.github.dockerjava.api.model.ExposedPort;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PortMapping {

    private final ExposedPort exposedPort;
    private final int hostPort;
}
