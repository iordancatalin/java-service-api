package com.online.compiler.runnerapi.features.multiversions;

import com.online.compiler.runnerapi.core.JavaVersionsManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class JavaVersionsRouter {

    private final JavaVersionsManager javaVersionsManager;

    @Bean
    public RouterFunction<ServerResponse> javaVersions() {
        return route().GET("/api/v1/java-versions", this::processRequest).build();
    }

    private Mono<ServerResponse> processRequest(ServerRequest serverRequest) {
        final var javaVersions = Mono.just(javaVersionsManager.getJavaVersions());

        return ServerResponse.ok().body(javaVersions, List.class);
    }
}
