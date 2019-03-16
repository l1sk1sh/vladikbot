package com.multiheaded.vladikbot.commands.music;

import com.multiheaded.vladikbot.VladikBot;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SoundCloudSearchCommand extends SearchCommand {
    public SoundCloudSearchCommand(VladikBot bot) {
        super(bot);
        this.searchPrefix = "scsearch:";
        this.name = "scsearch";
        this.help = "searches Soundcloud for a provided query";
        this.aliases = new String[]{};
    }
}
