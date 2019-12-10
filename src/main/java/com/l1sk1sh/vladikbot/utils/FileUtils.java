package com.l1sk1sh.vladikbot.utils;

import com.l1sk1sh.vladikbot.settings.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

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

    public static void deleteFilesByChannelIdAndExtension(String pathToDir, String id, String extension) {
        File f = new File(pathToDir);
        File[] paths = f.listFiles();

        if (paths != null) {
            for (File path : paths) {
                if ((path.toString().contains(id) && path.toString().contains(extension))
                        && !path.delete()) {
                    throw new SecurityException();
                }
            }
        }
    }

    public static void createFolderIfAbsent(String path) throws IOException {
        if (fileOrFolderIsAbsent(path)) {
            createFolders(path);
        }
    }

    public static boolean fileOrFolderIsAbsent(String path) {
        return !Files.exists(Paths.get(path));
    }

    public static void createFile(String path) throws IOException {
        Files.createFile(Paths.get(path));
    }

    public static void createFolders(String path) throws IOException {
        log.info("Creating folders {}", path);
        Files.createDirectories(Paths.get(path));
    }

    public static void deleteFile(String name) throws IOException {
        Files.delete(Paths.get(name));
    }

    public static String readFile(File file, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, encoding);
    }

    public static void writeSetToFile(String pathToFile, Set<String> set) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(pathToFile));

        for (String raw : set) {
            out.write(raw);
            out.newLine();
        }

        out.close();
    }
}
