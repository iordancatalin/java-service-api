package com.online.compiler.runnerapi.features.runner.service;

import com.github.dockerjava.api.model.ExposedPort;
import com.online.compiler.runnerapi.core.Constants;
import com.online.compiler.runnerapi.core.JavaVersionsManager;
import com.online.compiler.runnerapi.features.runner.model.BuildArgModel;
import com.online.compiler.runnerapi.features.runner.model.PortMapping;
import com.online.compiler.runnerapi.features.runner.model.RunnerRequestModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.SocketUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
@RequiredArgsConstructor
public class JavaRunnerService {

    @Value("${java-runner.docker.buildArgs.copyPathArg}")
    private String copyPathArg;

    @Value("${java-runner.docker.container.timeoutMilliseconds}")
    private Long containerTimeout;

    @Value("${gotty.port}")
    private Integer gottyPort;

    private final FileWriter fileWriter;
    private final JavaVersionsManager javaVersionsManager;
    private final DockerService dockerService;

    public CompletableFuture<Integer> writeJavaFileAndStartDockerContainer(RunnerRequestModel model) {
        return fileWriter.write(model.getCode())
                .thenApply(classDirectoryName -> buildDockerImage(model.getJavaVersion(), classDirectoryName))
                .thenApply(this::createDockerContainer);
    }

    private String buildDockerImage(String javaVersion, String classDirectoryName) {
        final var copyPath = Constants.GENERATED_DIR_RELATIVE_TO_STORAGE_ROOT + classDirectoryName;
        final var buildArg = new BuildArgModel(copyPathArg, copyPath);

        final var dockerfileName = javaVersionsManager.getDockerfileNameByJavaVersion(javaVersion);
        final var dockerFilePath =  Constants.STORAGE_ROOT + dockerfileName;

        return dockerService.buildImage(dockerFilePath, List.of(buildArg));
    }

    private Integer createDockerContainer(String imageId) {
        final var exposedPort = ExposedPort.tcp(gottyPort);
        final var freePort = SocketUtils.findAvailableTcpPort();
        final var portsMapping = List.of(new PortMapping(exposedPort, freePort));

        final var containerId = dockerService.createContainer(imageId, portsMapping);
        scheduleImageAndContainerForCleanUp(imageId, containerId);

        return freePort;
    }

    private void scheduleImageAndContainerForCleanUp(String imageId, String containerId) {
        final var cleanUpTask = new CleanUpTask(imageId, containerId);

        final var timer = new Timer();
        timer.schedule(cleanUpTask, containerTimeout);
    }

    @RequiredArgsConstructor
    private class CleanUpTask extends TimerTask {

        private final String imageId;
        private final String containerId;

        @Override
        public void run() {
            if (dockerService.isContainerAlive(containerId)) {
                dockerService.stopContainer(containerId);
            }

            dockerService.removeContainer(containerId);
            dockerService.removeImage(imageId, Boolean.TRUE);
        }
    }
}
