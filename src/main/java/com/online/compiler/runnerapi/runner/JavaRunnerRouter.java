package com.online.compiler.runnerapi.runner;

import com.online.compiler.runnerapi.runner.exception.CodeNotCompilableException;
import com.online.compiler.runnerapi.runner.model.ExecutionLog;
import com.online.compiler.runnerapi.runner.model.LogLevelEnum;
import com.online.compiler.runnerapi.runner.model.RunnerRequestModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.tools.Diagnostic;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static reactor.core.publisher.Mono.fromCompletionStage;
import static reactor.core.publisher.Mono.just;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class JavaRunnerRouter {

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
                .onErrorResume(CodeNotCompilableException.class, this::createBadRequestResponse);
    }

    private Mono<ServerResponse> createBadRequestResponse(CodeNotCompilableException exception) {
        final var message = exception.getDiagnosticCollector()
                .getDiagnostics()
                .stream()
                .map(Diagnostic::toString)
                .map(error -> new ExecutionLog(error, LogLevelEnum.ERROR))
                .collect(Collectors.toList());

        return ServerResponse.badRequest().body(just(message), List.class);
    }

    private Mono<ServerResponse> createOkResponse(List<ExecutionLog> executionLogs) {
        return ServerResponse.ok().body(just(executionLogs), List.class);
    }

    private Mono<List<ExecutionLog>> compileAndRunCode(String code) {
        return fromCompletionStage(javaRunnerService.compileAndExecuteCode(code));
    }
}
