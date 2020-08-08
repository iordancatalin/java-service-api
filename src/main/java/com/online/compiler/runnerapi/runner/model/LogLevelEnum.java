package com.online.compiler.runnerapi.runner.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public enum LogLevelEnum {

    INFO("SU5GTw=="),
    ERROR("RVJST1I=");

    private final String value;

    LogLevelEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<LogLevelEnum> fromValue(String value) {
        return Arrays.stream(LogLevelEnum.values())
                .filter(logLevel -> Objects.equals(logLevel.getValue(), value))
                .findFirst();
    }
}
