package com.multiheaded.vladikbot.commands.music;

import com.multiheaded.vladikbot.Bot;
import com.multiheaded.vladikbot.settings.Constants;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SoundCloudSearchCommand extends SearchCommand {
    public SoundCloudSearchCommand(Bot bot) {
        super(bot);
        this.name = "scsearch";
        this.help = "searches Soundcloud for a provided query";
        this.searchPrefix = Constants.SC_SEARCH_PREFIX;
    }
}
