package com.multiheaded.vladikbot.conductors.services;

import com.multiheaded.vladikbot.models.LockdownInterface;

import java.io.IOException;
import java.nio.file.*;

public class ClearTmpService {
    private final String localTmpPath;
    private final LockdownInterface lock;

    public ClearTmpService(String localTmpPath, LockdownInterface lock) {
        this.localTmpPath = localTmpPath;
        this.lock = lock;
    }

    public void clear() throws IOException, NullPointerException {
        try {
            lock.setAvailable(false);
            deleteDirectoryRecursion(Paths.get(localTmpPath));
        } finally {
            lock.setAvailable(true);
        }
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
