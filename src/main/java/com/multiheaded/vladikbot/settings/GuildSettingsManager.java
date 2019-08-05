package com.multiheaded.vladikbot.settings;

import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.multiheaded.vladikbot.settings.Constants.GUILD_SETTINGS_JSON;

/**
 * @author Oliver Johnson
 */
public class GuildSettingsManager extends AbstractSettingsManager
        implements com.jagrosh.jdautilities.command.GuildSettingsManager {

    private static final Logger logger = LoggerFactory.getLogger(GuildSettingsManager.class);
    private GuildSettings guildSettings;
    private final File guildConfigFile;

    public GuildSettingsManager() {
        guildConfigFile = new File(GUILD_SETTINGS_JSON);

        if (!guildConfigFile.exists()) {
            this.guildSettings = new GuildSettings(this);
            writeSettings();
            logger.warn(String.format("Created %s.", GUILD_SETTINGS_JSON));
        } else {
            try {
                this.guildSettings = gson.fromJson(
                        Files.readAllLines(guildConfigFile.toPath()).stream()
                                .map(String::trim)
                                .filter(s -> !s.startsWith("#") && !s.isEmpty())
                                .reduce((a, b) -> a += b)
                                .orElse(""),
                        GuildSettings.class
                );
            } catch (IOException e) {
                logger.error(String.format("Error while reading %s file.", GUILD_SETTINGS_JSON),
                        e.getLocalizedMessage(), e.getCause());
            }
        }
    }

    void writeSettings() {
        super.writeSettings(guildSettings, guildConfigFile);
    }

    @Override
    public Object getSettings(Guild guild) {
        return guildSettings;
    }
}