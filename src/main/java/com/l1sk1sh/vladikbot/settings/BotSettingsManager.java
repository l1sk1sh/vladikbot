package com.l1sk1sh.vladikbot.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;

import static com.l1sk1sh.vladikbot.settings.Constants.BOT_SETTINGS_JSON;

/**
 * @author Oliver Johnson
 */
public class BotSettingsManager extends AbstractSettingsManager {
    private static final Logger log = LoggerFactory.getLogger(BotSettingsManager.class);
    private BotSettings botSettings;
    private final File botConfigFile;

    public BotSettingsManager() {
        botConfigFile = new File(BOT_SETTINGS_JSON);

        if (!botConfigFile.exists()) {
            this.botSettings = new BotSettings(this);
            writeSettings();
            log.warn(String.format("Created %s. You will have to setup it manually", BOT_SETTINGS_JSON));
            System.exit(1);
        } else {
            try {
                this.botSettings = gson.fromJson(
                        Files.readAllLines(botConfigFile.toPath()).stream()
                                .map(String::trim)
                                .filter(s -> !s.startsWith("#") && !s.isEmpty())
                                .reduce((a, b) -> a += b)
                                .orElse(""),
                        BotSettings.class
                );
                this.botSettings.setManager(this);
            } catch (IOException e) {
                log.error(String.format("Error while reading %s file.", BOT_SETTINGS_JSON),
                        e.getLocalizedMessage(), e.getCause());
            }
        }
    }

    void writeSettings() {
        super.writeSettings(botSettings, botConfigFile);
    }

    public BotSettings getSettings() {
        return botSettings;
    }
}