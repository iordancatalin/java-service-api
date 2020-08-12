package com.online.compiler.runnerapi.runner.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.online.compiler.runnerapi.runner.model.BuildArgModel;
import com.online.compiler.runnerapi.runner.model.PortMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.List.of;

@Log4j2
@Service
@RequiredArgsConstructor
public class DockerService {

    private final DockerClient dockerClient;

    public String buildImage(String dockerFilePath) {
        return buildImage(dockerFilePath, of());
    }

    public String buildImage(String dockerfilePath, List<BuildArgModel> buildArgs) {
        Objects.requireNonNull(buildArgs);

        final var buildImage = dockerClient.buildImageCmd()
                .withDockerfile(new File(dockerfilePath))
                .withPull(Boolean.FALSE)
                .withNoCache(Boolean.FALSE);

        buildArgs.forEach(buildArg -> buildImage.withBuildArg(buildArg.getArgName(), buildArg.getArgValue()));

        return buildImage.exec(new BuildImageResultCallback())
                .awaitImageId();
    }

    public String createContainer(String imageId, List<PortMapping> portMappings) {
        final var exposedPorts = portMappings.stream()
                .map(PortMapping::getExposedPort)
                .collect(Collectors.toList());

        final var portBindings = new Ports();
        portMappings.forEach(portMap -> {
            final var binding = Ports.Binding.bindPort(portMap.getHostPort());
            portBindings.bind(portMap.getExposedPort(), binding);
        });

        final var createContainer = dockerClient.createContainerCmd(imageId)
                .withExposedPorts(exposedPorts)
                .withHostConfig(HostConfig.newHostConfig().withPortBindings(portBindings))
                .withName(UUID.randomUUID().toString())
                .exec();

        final var containerId = createContainer.getId();
        dockerClient.startContainerCmd(containerId).exec();

        return containerId;
    }

    public boolean isContainerAlive(String containerId) {
        return !dockerClient.listContainersCmd()
                .withIdFilter(of(containerId))
                .exec().isEmpty();
    }

    public void stopContainer(String containerId) {
        dockerClient.stopContainerCmd(containerId).exec();
    }

    public void removeImage(String imageId) {
        removeImage(imageId, Boolean.FALSE);
    }

    public void removeImage(String imageId, Boolean withForce) {
        dockerClient.removeImageCmd(imageId)
                .withForce(withForce).exec();
    }

    public void removeContainer(String containerId) {
        dockerClient.removeContainerCmd(containerId).exec();
    }
}
