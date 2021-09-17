package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * - Moving to JDA-Chewtils
 * @author John Grosh
 */
@Service
public class SoundCloudSearchCommand extends SearchCommand {

    @Autowired
    public SoundCloudSearchCommand(EventWaiter eventWaiter, GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager, BotSettingsManager settings) {
        super(eventWaiter, guildSettingsRepository, playerManager, settings);
        this.name = "msearchsoundcloud";
        this.help = "Searches Soundcloud for a provided query";
        this.searchPrefix = Const.SC_SEARCH_PREFIX;
    }
}
