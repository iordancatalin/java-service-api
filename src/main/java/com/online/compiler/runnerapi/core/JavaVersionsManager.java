package com.online.compiler.runnerapi.core;

import com.online.compiler.runnerapi.core.exception.DockerfileNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
public class JavaVersionsManager {

    public List<String> getJavaVersions() {
        final var files = getDockerfilesFromResources();

        return files.stream()
                .map(File::getName)
                .map(this::getJavaVersionFromFileName)
                .collect(Collectors.toList());
    }

    public String getDockerfileNameByJavaVersion(String javaVersion) {
        final var dockerfiles = getDockerfilesFromResources();

        return dockerfiles.stream()
                .map(File::getName)
                .filter(name -> checkFileJavaVersion(name, javaVersion))
                .findFirst()
                .orElseThrow(() -> new DockerfileNotFoundException(javaVersion));
    }

    private boolean checkFileJavaVersion(String fileName, String javaVersion) {
        final var fileJavaVersion = getJavaVersionFromFileName(fileName);
        return Objects.equals(javaVersion, fileJavaVersion);
    }

    @Cacheable("dockerfile")
    public List<File> getDockerfilesFromResources() {
        final var resource = new ClassPathResource("/dockerfile");

        try {
            final var files = resource.getFile().listFiles();

            if (Objects.nonNull(files)) {
                return Arrays.asList(files);
            }
        } catch (IOException e) {
            log.error(e);
        }

        return List.of();
    }

    private String getJavaVersionFromFileName(String fileName) {
        return fileName.split("\\.")[0];
    }
}
