package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.services.ClearTmpService;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author Oliver Johnson
 */
@Service
public class ClearTmpCommand extends OwnerCommand {
    private static final Logger log = LoggerFactory.getLogger(ClearTmpCommand.class);

    private final BotSettingsManager settings;
    private final ClearTmpService clearTmpService;

    @Autowired
    public ClearTmpCommand(ClearTmpService clearTmpService, BotSettingsManager settings) {
        this.settings = settings;
        this.clearTmpService = clearTmpService;
        this.name = "cleartmp";
        this.help = "completely clears tmp folder of the bot";
        this.guildOnly = false;
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            if (settings.get().isLockedBackup()) {
                event.replyWarning("Can't clear tmp folder, as it is being used by other process!");
                return;
            }
            event.replyWarning("Clearing temp folder...");

            clearTmpService.clear();
            log.info("'{}' folder was cleared.", settings.get().getLocalTmpFolder());
        } catch (IOException ioe) {
            log.error("Failed to clear '{}' folder.", settings.get().getLocalTmpFolder(), ioe);
            event.replyError(String.format("Something went wrong during clearing of tmp folder! `[%1$s][%2$s]`",
                    ioe.getClass().getSimpleName(), ioe.getLocalizedMessage()));
        }
    }
}
