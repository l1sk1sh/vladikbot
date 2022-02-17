package com.l1sk1sh.vladikbot.commands.owner;

import com.l1sk1sh.vladikbot.services.ShutdownHandler;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - Added permissions
 * - DI Spring
 * @author John Grosh
 */
@Slf4j
@Service
public class ShutdownCommand extends OwnerCommand {

    private final ShutdownHandler shutdownHandler;

    @Autowired
    public ShutdownCommand(ShutdownHandler shutdownHandler) {
        this.shutdownHandler = shutdownHandler;
        this.name = "shutdown";
        this.help = "Safely shuts down";
        this.guildOnly = false;
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        log.info("Bot is being shutdown by {}.", FormatUtils.formatAuthor(event));
        event.reply("Shutting down...").setEphemeral(true).queue();
        shutdownHandler.shutdown();
    }
}
