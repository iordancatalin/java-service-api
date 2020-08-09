package com.online.compiler.runnerapi.runner;

import com.online.compiler.runnerapi.runner.exception.CodeNotCompilableException;
import com.online.compiler.runnerapi.runner.model.ExecutionLog;
import com.online.compiler.runnerapi.runner.model.LogLevelEnum;
import com.online.compiler.runnerapi.runner.model.RunnerRequestModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.tools.Diagnostic;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static reactor.core.publisher.Mono.fromCompletionStage;
import static reactor.core.publisher.Mono.just;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class JavaRunnerRouter {

    @Value("${messages.timeout}")
    private String timeoutMessage;

    @Value("${timeout.duration}")
    private Integer timeout;

    @Value("${timeout.timeUnit}")
    private TimeUnit timeUnit;

    private final JavaRunnerService javaRunnerService;

    @Bean
    public RouterFunction<ServerResponse> javaRunner() {
        return route().POST("/api/v1/run-java", this::processRequest).build();
    }

    private Mono<ServerResponse> processRequest(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(RunnerRequestModel.class)
                .map(RunnerRequestModel::getCode)
                .flatMap(this::compileAndRunCode)
                .flatMap(this::createOkResponse)
                .onErrorResume(CodeNotCompilableException.class, this::createBadRequestResponse)
                .onErrorResume(TimeoutException.class, this::createTimeoutResponse);
    }

    private Mono<ServerResponse> createTimeoutResponse(TimeoutException timeoutException) {
        final var message = String.format(timeoutMessage, timeout, timeUnit);
        final var log = new ExecutionLog(message, LogLevelEnum.ERROR);

        return ServerResponse.ok().body(just(List.of(log)), List.class);
    }

    private Mono<ServerResponse> createBadRequestResponse(CodeNotCompilableException exception) {
        final var logs = exception.getDiagnosticCollector()
                .getDiagnostics()
                .stream()
                .map(Diagnostic::toString)
                .map(this::mapStringToErrorExecutionLog)
                .collect(Collectors.toList());

        return ServerResponse.badRequest().body(just(logs), List.class);
    }

    private ExecutionLog mapStringToErrorExecutionLog(String error) {
        return new ExecutionLog(error, LogLevelEnum.ERROR);
    }

    private Mono<ServerResponse> createOkResponse(List<ExecutionLog> executionLogs) {
        return ServerResponse.ok().body(just(executionLogs), List.class);
    }

    private Mono<List<ExecutionLog>> compileAndRunCode(String code) {
        return fromCompletionStage(javaRunnerService.compileAndExecuteCode(code));
    }
}
