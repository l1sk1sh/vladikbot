package com.multiheaded.vladikbot.conductors.services;

import com.multiheaded.vladikbot.settings.LockdownInterface;

import java.io.IOException;
import java.nio.file.*;

public class ClearTmpService {
    private final String localTmpPath;
    private LockdownInterface lock;

    public ClearTmpService(String localTmpPath, LockdownInterface lock) {
        this.localTmpPath = localTmpPath;
        this.lock = lock;
    }

    public void clear() throws IOException, NullPointerException {
        try {
            lock.setLockdown(true);
            deleteDirectoryRecursion(Paths.get(localTmpPath));
        } finally {
            lock.setLockdown(false);
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
