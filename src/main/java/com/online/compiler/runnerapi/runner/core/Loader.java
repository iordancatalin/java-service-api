package com.online.compiler.runnerapi.runner.core;

import com.online.compiler.runnerapi.common.Constants;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;

import static com.online.compiler.runnerapi.common.Constants.*;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Service
@Log4j2
public class Loader {

    public Optional<Class<?>> loadClass(String parentDirectoryName) {
        final var loader = createClassLoader(parentDirectoryName);

        if (loader == null) { return empty(); }

        try {
            return ofNullable(loader.loadClass(FILE_NAME));
        } catch (ClassNotFoundException e) {
            log.error(e);
        }

        return empty();
    }

    private URLClassLoader createClassLoader(String parentDirectoryName) {
        try {
            final var path = "./" + COMPILED_CLASSES_DIRECTORY + "/" + parentDirectoryName;

            return new URLClassLoader(new URL[]{new File(path).toURI().toURL()});
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
