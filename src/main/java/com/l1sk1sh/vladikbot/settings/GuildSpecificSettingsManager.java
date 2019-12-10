package com.l1sk1sh.vladikbot.settings;

import com.jagrosh.jdautilities.command.GuildSettingsManager;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 * @author Oliver Johnson
 */
public class GuildSpecificSettingsManager extends AbstractSettingsManager implements GuildSettingsManager<GuildSpecificSettings> {
    private static final Logger log = LoggerFactory.getLogger(GuildSpecificSettingsManager.class);

    private static final String GUILD_SETTINGS_JSON = "settings_guild.json";
    
    private GuildSpecificSettings guildSpecificSettings;
    private final File guildConfigFile;

    public GuildSpecificSettingsManager() {
        guildConfigFile = new File(GUILD_SETTINGS_JSON);

        if (!guildConfigFile.exists()) {
            this.guildSpecificSettings = new GuildSpecificSettings(this);
            writeSettings();
            log.warn(String.format("Created %1$s.", GUILD_SETTINGS_JSON));
        } else {
            try {
                this.guildSpecificSettings = gson.fromJson(
                        Files.readAllLines(guildConfigFile.toPath()).stream()
                                .map(String::trim)
                                .filter(s -> !s.startsWith("#") && !s.isEmpty())
                                .reduce((a, b) -> a += b)
                                .orElse(""),
                        GuildSpecificSettings.class
                );
                this.guildSpecificSettings.setManager(this);
            } catch (IOException e) {
                log.error(String.format("Error while reading %1$s file.", GUILD_SETTINGS_JSON),
                        e.getLocalizedMessage(), e.getCause());
            }
        }
    }

    final void writeSettings() {
        super.writeSettings(guildSpecificSettings, guildConfigFile);
    }

    @Override
    public GuildSpecificSettings getSettings(Guild guild) {
        return guildSpecificSettings;
    }
}