package com.multiheaded.vladikbot.conductors.services;

import java.io.IOException;
import java.nio.file.*;

public class ClearTmpService {
    private final String localTmpPath;

    public ClearTmpService(String localTmpPath) {
        this.localTmpPath = localTmpPath;
    }

    public void clear() throws IOException, NullPointerException {
        deleteDirectoryRecursion(Paths.get(localTmpPath));
    }

    private void deleteDirectoryRecursion(Path path) throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteDirectoryRecursion(entry);
                }
            }
        }
        Files.delete(path);
    }
}
