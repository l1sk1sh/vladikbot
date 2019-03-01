package com.multiheaded.disbot.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;

import static com.multiheaded.disbot.settings.Constants.CONFIG_NAME;

public class SettingsManager {
    private static final Logger logger = LoggerFactory.getLogger(SettingsManager.class);
    private static SettingsManager instance;
    private Settings settings;

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    private SettingsManager(){
        File confFile = new File(CONFIG_NAME);

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            if (!confFile.exists()) {
                this.settings = new Settings();
                JsonWriter writer = new JsonWriter(new FileWriter(confFile));
                writer.setIndent("  ");
                writer.setHtmlSafe(false);
                gson.toJson(settings, Settings.class, writer);
                writer.close();
                logger.warn(String.format("Created %s. You will have to setup it manually", CONFIG_NAME));
                System.exit(Constants.NEWLY_CREATED_CONFIG);
            } else {
                this.settings = gson.fromJson(
                        Files.readAllLines(confFile.toPath()).stream()
                                .map(String::trim)
                                .filter(s -> !s.startsWith("#") && !s.isEmpty())
                                .reduce((a, b) -> a += b)
                                .orElse(""),
                        Settings.class
                );
            }
        } catch (IOException e) {
            logger.error(String.format("Error while reading/creating %s file.", CONFIG_NAME),
                    e.getMessage(), e.getCause());
        }
    }

    public Settings getSettings() {
        return settings;
    }
}