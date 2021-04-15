package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

/**
 * @author Oliver Johnson
 */
@Service
public class ClearTmpService {
    private final BotSettingsManager settings;
    private final String localTmpPath;

    @Autowired
    public ClearTmpService(BotSettingsManager settings) {
        this.settings = settings;
        this.localTmpPath = this.settings.get().getLocalTmpFolder();
    }

    public final void clear() throws IOException {
        try {
            settings.get().setLockedBackup(true);
            deleteDirectoryRecursion(Paths.get(localTmpPath));
        } finally {
            settings.get().setLockedBackup(false);
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
