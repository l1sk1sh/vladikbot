package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.services.ShutdownHandler;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Service
public class ShutdownCommand extends OwnerCommand {
    private static final Logger log = LoggerFactory.getLogger(ShutdownCommand.class);

    private final ShutdownHandler shutdownHandler;

    @Autowired
    public ShutdownCommand(ShutdownHandler shutdownHandler) {
        this.shutdownHandler = shutdownHandler;
        this.name = "shutdown";
        this.help = "safely shuts down";
        this.guildOnly = false;
    }

    @Override
    protected final void execute(CommandEvent event) {
        log.info("Bot is being shutdown by {}.", FormatUtils.formatAuthor(event));
        event.replyWarning("Shutting down...");
        shutdownHandler.shutdown();
    }
}
