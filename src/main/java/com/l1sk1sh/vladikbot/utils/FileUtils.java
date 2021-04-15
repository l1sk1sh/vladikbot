package com.l1sk1sh.vladikbot.utils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.l1sk1sh.vladikbot.settings.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;

/**
 * @author Oliver Johnson
 */
public final class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {}

    public static File getFileByChannelIdAndExtension(String pathToDir, String channelId, Const.FileType extension) throws IOException {
        Files.createDirectories(Paths.get(pathToDir));
        File folder = new File(pathToDir);
        File[] paths = folder.listFiles();
        File file = null;

        if (paths != null) {
            for (File path : paths) {
                if (path.toString().contains(channelId) && path.toString().contains("." + extension.name())) {
                    if (file != null) {
                        file = (file.lastModified() > path.lastModified()) ? file : path;
                    } else {
                        file = path;
                    }
                }
            }
        }

        return file;
    }

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
