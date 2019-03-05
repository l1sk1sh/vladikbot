package com.multiheaded.disbot.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Objects;

public class FileUtils {
    public static String getFileNameByIdAndExtension(String pathToDir, String id, String extension) {
        File f = new File(pathToDir);
        File[] paths = f.listFiles();

        for(File path : Objects.requireNonNull(paths)) {
            if (path.toString().contains(id) && path.toString().contains(extension))
                return path.toString();
        }

        return null;
    }

    public static String readFile(File file, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, encoding);
    }
}
