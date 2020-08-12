package com.online.compiler.runnerapi.common;

public class Constants {

    public static final String CLASSES_DIRECTORY;
    public static final String FILE_NAME_WITH_JAVA_EXTENSION = "Main.java";

    static {
        final var userHome = System.getProperty("user.home");
        final var sanitizedUserHome = userHome.replaceAll("\\\\", "/");

        CLASSES_DIRECTORY = sanitizedUserHome + "/JavaOnlineCompiler/generated/classes";
    }
}
