package com.online.compiler.runnerapi.common;

public class Constants {

    public static final String COMPILED_CLASSES_DIRECTORY;
    public static final String FILE_NAME_WITH_JAVA_EXTENSION = "Main.java";
    public static final String FILE_NAME = "Main";

    static {
        final String userHome = System.getProperty("user.home");
        final var sanitizedUserHome = userHome.replaceAll("\\\\", "/");

        COMPILED_CLASSES_DIRECTORY = sanitizedUserHome + "/JavaOnlineCompiler/generated/classes";
    }
}
