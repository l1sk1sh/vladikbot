package com.l1sk1sh.vladikbot.utils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;

/**
 * @author l1sk1sh
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

    public static void createFolderIfAbsent(String path) throws IOException {
        if (fileOrFolderIsAbsent(path)) {
            log.info("Creating folders '{}'...", path);
            Files.createDirectories(Paths.get(path));
        }
    }

    public static boolean fileOrFolderIsAbsent(String path) throws InvalidPathException {
        return !Files.exists(Paths.get(path));
    }

    public static boolean isDirEmpty(final Path directory) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    public static String readFile(File file, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, encoding);
    }

    public static void writeJson(Object object, File configFile, Gson gson) throws IOException {
        try (JsonWriter writer = new JsonWriter(new FileWriter(configFile))) {
            writer.setIndent("  ");
            writer.setHtmlSafe(false);
            gson.toJson(object, object.getClass(), writer);
        }
    }
}
