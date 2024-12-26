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
 */
@Service
public class SpotifySearchCommand extends SearchCommand {

    @Autowired
    public SpotifySearchCommand(EventWaiter eventWaiter, GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager, BotSettingsManager settings) {
        super(eventWaiter, guildSettingsRepository, playerManager, settings);
        this.name = "msearch_spotify";
        this.help = "Searches Spotify for a provided query";
        this.searchPrefix = Const.SP_SEARCH_PREFIX;
    }
}
