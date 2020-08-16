package com.online.compiler.runnerapi.features.runner.service;

import com.online.compiler.runnerapi.core.Constants;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j2
@Configuration
@EnableScheduling
public class CleanUpScheduledJob {

    @Scheduled(fixedRate = 43_200_000L)
    public void cleanUpJob() {
        try {
            Files.walk(Paths.get(Constants.CLASSES_DIRECTORY))
                    .map(Path::toFile)
                    .skip(1)
                    .sorted((o1, o2) -> -o1.compareTo(o2))
                    .forEach(File::delete);
        } catch (IOException e) {
            log.error(e);
        }
    }
}
