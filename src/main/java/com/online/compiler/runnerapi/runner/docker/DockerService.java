package com.online.compiler.runnerapi.runner.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.SocketUtils;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static java.util.List.of;

@Log4j2
@Service
@RequiredArgsConstructor
public class DockerService {

    @Value("${java-runner.docker.filePath}")
    private String dockerFilePath;

    @Value("${java-runner.docker.buildArgs.copyPathArg}")
    private String copyPathArg;

    @Value("${java-runner.docker.relativeCopyPath}")
    private String relativeCopyPath;

    @Value("${java-runner.docker.container.timeoutMilliseconds}")
    private Long containerTimeout;

    @Value("${gotty.port}")
    private Integer gottyPort;

    private final DockerClient dockerClient;

    /**
     * @param classDirectoryName
     * @return the port to witch the user can see the terminal
     */
    public int startTerminal(String classDirectoryName) {
        final var freePort = SocketUtils.findAvailableTcpPort();
        final var tcp8080 = ExposedPort.tcp(gottyPort);

        final var portBindings = new Ports();
        portBindings.bind(tcp8080, Ports.Binding.bindPort(freePort));

        final var copyPath = relativeCopyPath + classDirectoryName;
        final var imageId = dockerClient.buildImageCmd()
                .withDockerfile(new File(dockerFilePath))
                .withBuildArg(copyPathArg, copyPath)
                .withPull(Boolean.FALSE)
                .withNoCache(Boolean.TRUE)
                .exec(new BuildImageResultCallback())
                .awaitImageId();

        final var createContainer = dockerClient.createContainerCmd(imageId)
                .withExposedPorts(tcp8080)
                .withHostConfig(HostConfig.newHostConfig().withPortBindings(portBindings))
                .withName(UUID.randomUUID().toString())
                .exec();

        dockerClient.startContainerCmd(createContainer.getId()).exec();
        scheduleContainerForKill(createContainer.getId());

        return freePort;
    }

    private void scheduleContainerForKill(String containerId) {
        final var killerTask = new ContainerKiller(dockerClient, containerId);

        final var timer = new Timer();
        timer.schedule(killerTask, containerTimeout);
    }

    @RequiredArgsConstructor
    private static class ContainerKiller extends TimerTask {

        private final DockerClient dockerClient;
        private final String containerId;

        @Override
        public void run() {
            if (isContainerAlive()) {
                dockerClient.stopContainerCmd(containerId).exec();
            }
        }

        private boolean isContainerAlive() {
            return !dockerClient.listContainersCmd()
                    .withIdFilter(of(containerId))
                    .exec().isEmpty();
        }
    }
}
