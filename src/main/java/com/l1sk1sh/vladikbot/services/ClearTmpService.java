package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.models.LockService;

import java.io.IOException;
import java.nio.file.*;

public class ClearTmpService {
    private final String localTmpPath;
    private final LockService lock;

    public ClearTmpService(String localTmpPath, LockService lock) {
        this.localTmpPath = localTmpPath;
        this.lock = lock;
    }

    public final void clear() throws IOException {
        try {
            lock.setLocked(true);
            deleteDirectoryRecursion(Paths.get(localTmpPath));
        } finally {
            lock.setLocked(false);
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
