package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.Bot;

import java.io.IOException;
import java.nio.file.*;

public class ClearTmpService {
    private final String localTmpPath;
    private final Bot bot;

    public ClearTmpService(Bot bot) {
        this.bot = bot;
        this.localTmpPath = bot.getBotSettings().getLocalTmpPath();
    }

    public final void clear() throws IOException {
        try {
            bot.setLockedBackup(true);
            deleteDirectoryRecursion(Paths.get(localTmpPath));
        } finally {
            bot.setLockedBackup(false);
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
