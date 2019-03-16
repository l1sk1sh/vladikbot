package com.multiheaded.vladikbot.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Objects;

/**
 * @author Oliver Johnson
 */
public class FileUtils {
    public static File getFileByIdAndExtension(String pathToDir, String id, String extension) {
        File folder = new File(pathToDir);
        File[] paths = folder.listFiles();
        File file = null;

        for (File path : Objects.requireNonNull(paths)) {
            if (path.toString().contains(id) && path.toString().contains(extension)) {
                if (file != null) {
                    file = (file.lastModified() > path.lastModified()) ? file : path;
                } else {
                    file = path;
                }
            }
        }

        return file;
    }

    public static String readFile(File file, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, encoding);
    }

    public static void deleteFilesByIdAndExtension(String pathToDir, String id, String extension) {
        File f = new File(pathToDir);
        File[] paths = f.listFiles();

        for(File path : Objects.requireNonNull(paths)) {
            if (path.toString().contains(id) && path.toString().contains(extension))
                if (!path.delete()) {
                    throw new SecurityException();
                }
        }
    }
}
