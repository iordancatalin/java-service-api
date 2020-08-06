package com.online.compiler.runnerapi.runner.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

@Getter
@RequiredArgsConstructor
public class CodeNotCompilableException extends RuntimeException {

    private final DiagnosticCollector<JavaFileObject> diagnosticCollector;
}
