package com.online.compiler.runnerapi.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Log4j2
@Component
@RequiredArgsConstructor
public class ApplicationReadyListener {

    private final JavaVersionsManager javaVersionsManager;

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReady() {
        final var dockerfiles = javaVersionsManager.getDockerfiles();

        dockerfiles.stream()
                .map(File::toPath)
                .forEach(this::copyFileToStorageRoot);
    }

    private void copyFileToStorageRoot(Path source) {
        final var sourceFileName = source.getFileName().toString();
        final var destination = Path.of(Constants.STORAGE_ROOT, sourceFileName);

        try {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error(e);
        }
    }
}
