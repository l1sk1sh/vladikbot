package com.l1sk1sh.vladikbot.settings;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author l1sk1sh
 */
@Service
public class BotSettingsManager implements SettingsUpdateListener {
    private static final Logger log = LoggerFactory.getLogger(BotSettingsManager.class);

    public static final String DEFAULT_SETTINGS_DIR = "./";
    public static final String BOT_SETTINGS_JSON = "settings_bot.json";

    private final Gson gson;
    private BotSettings botSettings;
    private final File botConfigFile;

    @Autowired
    public BotSettingsManager(Gson gson) {
        this.gson = gson;
        this.botConfigFile = new File(DEFAULT_SETTINGS_DIR + "/" + BOT_SETTINGS_JSON);
        this.botSettings = new BotSettings(this);
    }

    public void readSettings() throws IOException {
        if (!botConfigFile.exists()) {
            writeSettings();
            log.warn(String.format("Created %1$s. You will have to setup it manually", BOT_SETTINGS_JSON));
            SystemUtils.exit(1);
        } else {
            this.botSettings = gson.fromJson(
                    Files.readAllLines(botConfigFile.toPath()).stream()
                            .map(String::trim)
                            .filter(s -> !s.startsWith("#") && !s.isEmpty())
                            .reduce((a, b) -> a += b)
                            .orElse(""),
                    BotSettings.class
            );
            this.botSettings.setListener(this);
        }
    }

    private void writeSettings() {
        try {
            FileUtils.writeJson(botSettings, botConfigFile, gson);
        } catch (IOException e) {
            log.error("Failed to write BotSettings. Application might still be working.", e);
        }
    }

    public synchronized BotSettings get() {
        return botSettings;
    }

    @Override
    public void onSettingsUpdated() {
        writeSettings();
    }

    /**
     * "Hacker" way of reading token fod JDA initialization to avoid circular dependency
     *
     * @return String with token form settings_bot.json
     * @throws IOException for any IO exception
     */
    public static String readRawToken() throws IOException {
        File settingsFile = new File(DEFAULT_SETTINGS_DIR + "/" + BOT_SETTINGS_JSON);
        if (!settingsFile.exists()) {
            FileUtils.writeJson(new BotSettings(null), settingsFile, new Gson());
        }
        JsonElement json = JsonParser.parseReader(new FileReader(settingsFile));
        if (json.isJsonObject()) {
            return json.getAsJsonObject().get("token").getAsString();
        }
        return null;
    }
}