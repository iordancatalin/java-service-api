package com.online.compiler.runnerapi.core.exception;

public class DockerfileNotFoundException extends RuntimeException{

    private static final String message = "No dockerfile found for %s";

    public DockerfileNotFoundException( String javaVersion) {
        super(String.format(message, javaVersion));
    }
}
