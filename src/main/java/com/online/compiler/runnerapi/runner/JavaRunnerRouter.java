package com.online.compiler.runnerapi.runner;

import com.online.compiler.runnerapi.runner.core.CodeExecutor;
import com.online.compiler.runnerapi.runner.model.ExecutionResult;
import com.online.compiler.runnerapi.runner.model.RunnerRequestModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class JavaRunnerRouter {

    private final CodeExecutor codeExecutor;

    @Bean
    public RouterFunction<ServerResponse> javaRunner() {
        return route().POST("/api/v1/run-java", this::processRequest).build();
    }

    private Mono<ServerResponse> processRequest(ServerRequest serverRequest) {

        final var bodyPublisher = serverRequest.bodyToMono(RunnerRequestModel.class)
                .map(RunnerRequestModel::getCode)
                .flatMap(this::runCode)
                .doOnError(log::error);

        return ServerResponse.ok().body(bodyPublisher, ExecutionResult.class);
    }

    private Mono<String> runCode(String code) {
        return Mono.fromCompletionStage(codeExecutor.executeCode(code));
    }
}
