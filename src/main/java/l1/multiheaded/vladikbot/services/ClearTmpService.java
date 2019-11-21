package l1.multiheaded.vladikbot.services;

import l1.multiheaded.vladikbot.models.LockService;

import java.io.IOException;
import java.nio.file.*;

public class ClearTmpService {
    private final String localTmpPath;
    private final LockService lock;

    public ClearTmpService(String localTmpPath, LockService lock) {
        this.localTmpPath = localTmpPath;
        this.lock = lock;
    }

    public void clear() throws IOException, NullPointerException {
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
