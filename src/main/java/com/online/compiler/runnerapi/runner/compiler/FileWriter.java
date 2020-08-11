package com.online.compiler.runnerapi.runner.compiler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.online.compiler.runnerapi.common.Constants.*;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

@Service
@RequiredArgsConstructor
public class FileWriter {

    /**
     * @param code the java code to write in file
     * @return CompletableFuture with UUID of the parent directory
     */
    public CompletableFuture<String> write(String code) {
        final var cfResult = new CompletableFuture<String>();

        try {
            final var codeDirectoryName = String.valueOf(UUID.randomUUID());
            final var directoriesPath = Path.of(getDirectoriesPath(codeDirectoryName));

            createDirectoriesIfNotExists(directoriesPath);

            final var pathToClass = Path.of(directoriesPath.toString(), FILE_NAME_WITH_JAVA_EXTENSION);
            final var buffer = createBufferFromCode(code);
            final var completionHandler = new FileWriteCompletionHandler(codeDirectoryName);

            try (final var channel = AsynchronousFileChannel.open(pathToClass, CREATE, WRITE)) {
                channel.write(buffer, 0, cfResult, completionHandler);
            }

        } catch (Exception e) {
            cfResult.completeExceptionally(e);
        }

        return cfResult;
    }

    private void createDirectoriesIfNotExists(Path directoriesPath) throws IOException {
        if (!Files.exists(directoriesPath)) {
            Files.createDirectories(directoriesPath);
        }
    }

    private String getDirectoriesPath(String uuid) {
        return COMPILED_CLASSES_DIRECTORY + "/" + uuid + "/";
    }

    private ByteBuffer createBufferFromCode(String code) {
        final var bytes = code.getBytes();
        final var buffer = ByteBuffer.allocate(bytes.length);

        buffer.put(bytes);
        buffer.flip();

        return buffer;
    }

    @RequiredArgsConstructor
    private static class FileWriteCompletionHandler implements CompletionHandler<Integer, CompletableFuture<String>> {
        private final String parentDirectoryName;

        @Override
        public void completed(Integer result, CompletableFuture<String> attachment) {
            attachment.complete(parentDirectoryName);
        }

        @Override
        public void failed(Throwable exc, CompletableFuture<String> attachment) {
            attachment.completeExceptionally(exc);
        }
    }
}
