package com.online.compiler.runnerapi.core;

import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
@Service
public class JavaVersionsManager {

    public List<String> getJavaVersions() {
        final var files = getDockerfiles();

        return files.stream()
                .map(File::getName)
                .map(this::getJavaVersionFromFileName)
                .collect(Collectors.toList());
    }

    @Cacheable("dockerfile")
    public List<File> getDockerfiles() {
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
