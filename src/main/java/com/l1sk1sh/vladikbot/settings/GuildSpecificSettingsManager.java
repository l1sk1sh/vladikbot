package com.l1sk1sh.vladikbot.settings;

import com.jagrosh.jdautilities.command.GuildSettingsManager;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Oliver Johnson
 */
public class GuildSpecificSettingsManager implements GuildSettingsManager<GuildSpecificSettings> {
    private static final Logger log = LoggerFactory.getLogger(GuildSpecificSettingsManager.class);

    private static final String GUILD_SETTINGS_JSON = "settings_guild.json";

    private GuildSpecificSettings guildSpecificSettings;
    private final File guildConfigFile;

    public GuildSpecificSettingsManager() {
        this.guildConfigFile = new File(GUILD_SETTINGS_JSON);
        this.guildSpecificSettings = new GuildSpecificSettings(this);
    }

    public void readSettings() throws IOException {
        if (!guildConfigFile.exists()) {
            writeSettings();
            log.warn(String.format("Created %1$s.", GUILD_SETTINGS_JSON));
        } else {
            this.guildSpecificSettings = Bot.gson.fromJson(
                    Files.readAllLines(guildConfigFile.toPath()).stream()
                            .map(String::trim)
                            .filter(s -> !s.startsWith("#") && !s.isEmpty())
                            .reduce((a, b) -> a += b)
                            .orElse(""),
                    GuildSpecificSettings.class
            );
            this.guildSpecificSettings.setManager(this);
        }
    }

    final void writeSettings() {
        try {
            FileUtils.writeGson(guildSpecificSettings, guildConfigFile);
        } catch (IOException e) {
            log.error("Failed to write GuildSpecificSettings. Application might still be working.", e);
        }
    }

    @Override
    public GuildSpecificSettings getSettings(Guild guild) {
        return guildSpecificSettings;
    }
}