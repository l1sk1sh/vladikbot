package com.l1sk1sh.vladikbot.settings;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class OfflineStorageManager {
    private static final Logger log = LoggerFactory.getLogger(OfflineStorageManager.class);

    private static final String OFFLINE_STORAGE_JSON = "storage.json";

    private OfflineStorage offlineStorage;
    private final File offlineStorageFile;

    public OfflineStorageManager() {
        this.offlineStorageFile = new File(OFFLINE_STORAGE_JSON);
        this.offlineStorage = new OfflineStorage(this);
    }

    public void readSettings() throws IOException {
        if (!offlineStorageFile.exists()) {
            writeSettings();
            log.warn(String.format("Created %1$s.", OFFLINE_STORAGE_JSON));
        } else {
            this.offlineStorage = Bot.gson.fromJson(
                    Files.readAllLines(offlineStorageFile.toPath()).stream()
                            .map(String::trim)
                            .filter(s -> !s.startsWith("#") && !s.isEmpty())
                            .reduce((a, b) -> a += b)
                            .orElse(""),
                    OfflineStorage.class
            );
            this.offlineStorage.setManager(this);
        }
    }

    final void writeSettings() {
        try {
            FileUtils.writeGson(offlineStorage, offlineStorageFile);
        } catch (IOException e) {
            log.error("Failed to write OfflineStorageSettings. Application might still be working.", e);
        }
    }

    public OfflineStorage getSettings() {
        return offlineStorage;
    }
}
