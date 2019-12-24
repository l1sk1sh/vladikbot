package com.l1sk1sh.vladikbot.settings;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;

/**
 * @author Oliver Johnson
 */
public class BotSettingsManager {
    private static final Logger log = LoggerFactory.getLogger(BotSettingsManager.class);
    
    private static final String BOT_SETTINGS_JSON = "settings_bot.json";
    
    private BotSettings botSettings;
    private final File botConfigFile;

    public BotSettingsManager() {
        this.botConfigFile = new File(BOT_SETTINGS_JSON);
        this.botSettings = new BotSettings(this);
    }

    public void readSettings() throws IOException {
        if (!botConfigFile.exists()) {
            writeSettings();
            log.warn(String.format("Created %1$s. You will have to setup it manually", BOT_SETTINGS_JSON));
            SystemUtils.exit(1, 5000);
        } else {
            this.botSettings = Bot.gson.fromJson(
                    Files.readAllLines(botConfigFile.toPath()).stream()
                            .map(String::trim)
                            .filter(s -> !s.startsWith("#") && !s.isEmpty())
                            .reduce((a, b) -> a += b)
                            .orElse(""),
                    BotSettings.class
            );
            this.botSettings.setManager(this);
        }
    }

    final void writeSettings() {
        try {
            FileUtils.writeGson(botSettings, botConfigFile);
        } catch (IOException e) {
            log.error("Failed to write BotSettings. Application might still be working.", e);
        }
    }

    public BotSettings getSettings() {
        return botSettings;
    }
}