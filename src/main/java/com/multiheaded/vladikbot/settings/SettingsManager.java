package com.multiheaded.vladikbot.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;

import static com.multiheaded.vladikbot.settings.Constants.SETTINGS_JSON;

/**
 * @author Oliver Johnson
 */
public class SettingsManager {
    private static final Logger logger = LoggerFactory.getLogger(SettingsManager.class);
    private static final SettingsManager instance = new SettingsManager();
    private Settings settings;
    private final File confFile;
    private final Gson gson;

    private SettingsManager() {
        confFile = new File(SETTINGS_JSON);

        gson = new GsonBuilder().setPrettyPrinting().create();
        if (!confFile.exists()) {
            this.settings = new Settings();
            writeSettings();
            logger.warn(String.format("Created %s. You will have to setup it manually", SETTINGS_JSON));
            System.exit(1);
        } else {
            try {
                this.settings = gson.fromJson(
                        Files.readAllLines(confFile.toPath()).stream()
                                .map(String::trim)
                                .filter(s -> !s.startsWith("#") && !s.isEmpty())
                                .reduce((a, b) -> a += b)
                                .orElse(""),
                        Settings.class
                );
            } catch (IOException e) {
                logger.error(String.format("Error while reading %s file.", SETTINGS_JSON),
                        e.getMessage(), e.getCause());
            }
        }
    }

    void writeSettings() {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(confFile));
            writer.setIndent("  ");
            writer.setHtmlSafe(false);
            gson.toJson(settings, Settings.class, writer);
            writer.close();
        } catch (IOException e) {
            logger.error(String.format("Error while writing %s file.", SETTINGS_JSON),
                    e.getMessage(), e.getCause());
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public static SettingsManager getInstance() {
        return instance;
    }
}