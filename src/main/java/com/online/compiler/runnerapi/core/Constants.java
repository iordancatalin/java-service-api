package com.online.compiler.runnerapi.core;

public class Constants {

    public static final String STORAGE_ROOT;

    private static final String GENERATED_DIRECTORY = "generated";
    public static final String GENERATED_DIR_RELATIVE_TO_STORAGE_ROOT = "./" + GENERATED_DIRECTORY + "/";
    public static final String CLASSES_DIRECTORY;
    public static final String FILE_NAME_WITH_JAVA_EXTENSION = "Main.java";

    static {
        final var userHome = System.getProperty("user.home");
        final var sanitizedUserHome = userHome.replaceAll("\\\\", "/");

        STORAGE_ROOT = sanitizedUserHome + "/JavaOnlineCompiler/";
        CLASSES_DIRECTORY = STORAGE_ROOT + GENERATED_DIRECTORY;
    }
}
