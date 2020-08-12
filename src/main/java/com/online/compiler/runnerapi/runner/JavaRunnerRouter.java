package com.online.compiler.runnerapi.runner;

import com.online.compiler.runnerapi.runner.model.RunnerRequestModel;
import com.online.compiler.runnerapi.runner.model.TerminalStartModel;
import com.online.compiler.runnerapi.runner.service.JavaRunnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

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
                .flatMap(this::createOkResponse);
    }

    private Mono<ServerResponse> createOkResponse(Integer port) {
        final var gottyEndpoint = "http://localhost:" + port;
        final var body = new TerminalStartModel(gottyEndpoint);

        return ServerResponse.ok().body(just(body), TerminalStartModel.class);
    }

    private Mono<Integer> compileAndRunCode(String code) {
        return fromCompletionStage(javaRunnerService.runCode(code));
    }
}
