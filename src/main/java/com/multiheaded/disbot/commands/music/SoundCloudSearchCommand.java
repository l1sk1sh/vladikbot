package com.multiheaded.disbot.commands.music;

import com.multiheaded.disbot.Bot;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SoundCloudSearchCommand extends SearchCommand {
    public SoundCloudSearchCommand(Bot bot, String searchingEmoji) {
        super(bot, searchingEmoji);
        this.searchPrefix = "scsearch:";
        this.name = "scsearch";
        this.help = "searches Soundcloud for a provided query";
        this.aliases = new String[]{};
    }
}
