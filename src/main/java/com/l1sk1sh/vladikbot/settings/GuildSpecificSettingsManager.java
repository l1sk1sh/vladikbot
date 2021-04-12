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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oliver Johnson
 */
public class GuildSpecificSettingsManager implements GuildSettingsManager<GuildSpecificSettings> {
    private static final Logger log = LoggerFactory.getLogger(GuildSpecificSettingsManager.class);

    private static final String GUILD_SETTINGS_JSON = "settings_guild.json";

    private final String settingsPath;
    private final Map<String, GuildSpecificSettings> guildSpecificSettings;

    public GuildSpecificSettingsManager(String settingsPath) {
        this.settingsPath = settingsPath;
        this.guildSpecificSettings = new HashMap<>();
    }

    private GuildSpecificSettings readSettings(Guild guild) {
        File guildConfigFile = getSettingsFileForGuild(guild.getId());
        GuildSpecificSettings settingsForGuild = new GuildSpecificSettings(guild.getId(), this);

        if (!guildConfigFile.exists()) {
            writeSettings(settingsForGuild);
            guildSpecificSettings.put(guild.getId(), settingsForGuild);
            log.warn(String.format("Created %1$s.", guildConfigFile.getName()));
        } else {
            try {
                settingsForGuild = Bot.gson.fromJson(
                        Files.readAllLines(guildConfigFile.toPath()).stream()
                                .map(String::trim)
                                .filter(s -> !s.startsWith("#") && !s.isEmpty())
                                .reduce((a, b) -> a += b)
                                .orElse(""),
                        GuildSpecificSettings.class
                );
                settingsForGuild.setManager(this);
                guildSpecificSettings.put(guild.getId(), settingsForGuild);
            } catch (IOException e) {
                log.error("Failed to read GuildSpecificSettings. Application might still be working.", e);
            }
        }

        return settingsForGuild;
    }

    final void writeSettings(GuildSpecificSettings settings) {
        try {
            FileUtils.createFolderIfAbsent(settingsPath);
            FileUtils.writeGson(settings, getSettingsFileForGuild(settings.getGuildId()));
        } catch (IOException e) {
            log.error("Failed to write GuildSpecificSettings. Application might still be working.", e);
        }
    }

    @Override
    public GuildSpecificSettings getSettings(Guild guild) {
        GuildSpecificSettings settingsForGuild = guildSpecificSettings.get(guild.getId());

        if (settingsForGuild == null) {
            settingsForGuild = readSettings(guild);
        }

        return settingsForGuild;
    }

    private File getSettingsFileForGuild(String guildId) {
        return new File(settingsPath + guildId + "_" + GUILD_SETTINGS_JSON);
    }
}