package com.online.compiler.runnerapi.runner.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerConfig {

    @Bean
    public DockerClientConfig createConfig() {
        return DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://localhost:2375")
                .build();
    }

    @Bean
    public DockerHttpClient createHttpClient(DockerClientConfig config) {
        return new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
//                .sslConfig(config.getSSLConfig())
                .build();
    }

    @Bean
    public DockerClient dockerClient(DockerClientConfig dockerConfig, DockerHttpClient dockerHttpClient) {
        return DockerClientImpl.getInstance(dockerConfig, dockerHttpClient);
    }
}
