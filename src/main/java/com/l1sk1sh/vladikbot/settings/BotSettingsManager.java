package com.l1sk1sh.vladikbot.settings;

import com.google.gson.Gson;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.SystemUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author l1sk1sh
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BotSettingsManager implements SettingsUpdateListener {

    public static final String DEFAULT_SETTINGS_DIR = "./";
    public static final String BOT_SETTINGS_JSON = "settings_bot.json";

    private final Gson gson;
    private BotSettings botSettings = new BotSettings(this);
    private final File botConfigFile = new File(DEFAULT_SETTINGS_DIR + "/" + BOT_SETTINGS_JSON);

    public void init() throws IOException {
        if (!botConfigFile.exists()) {
            writeSettings();
            log.warn(String.format("Created %1$s. You will have to setup it manually", BOT_SETTINGS_JSON));
            SystemUtils.exit(1);
        } else {
            this.botSettings = gson.fromJson(
                    Files.readAllLines(botConfigFile.toPath()).stream()
                            .map(String::trim)
                            .filter(s -> !s.startsWith("#") && !s.isEmpty())
                            .reduce((a, b) -> a + b)
                            .orElse(""),
                    BotSettings.class
            );
            this.botSettings.setListener(this);
            this.botSettings.updateMissingValues();
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
}