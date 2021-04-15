package com.l1sk1sh.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class RepeatCommand extends DJCommand {

    private final BotSettingsManager settings;

    @Autowired
    public RepeatCommand(BotSettingsManager settings, GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.settings = settings;
        this.name = "repeat";
        this.help = "re-adds music to the queue when finished";
        this.arguments = "<on|off>";
        this.guildOnly = true;
    }

    /* Override MusicCommand's execute because we don't actually care where this is used */
    @Override
    protected final void execute(CommandEvent event) {
        boolean value;
        if (event.getArgs().isEmpty()) {
            value = !settings.get().isRepeat();
        } else if (event.getArgs().equalsIgnoreCase("true") || event.getArgs().equalsIgnoreCase("on")) {
            value = true;
        } else if (event.getArgs().equalsIgnoreCase("false") || event.getArgs().equalsIgnoreCase("off")) {
            value = false;
        } else {
            event.replyError("Valid options are `on` or `off` (or leave empty to toggle).");
            return;
        }
        settings.get().setRepeat(value);
        event.replySuccess(String.format("Repeat mode is now `%1$s`.", (value ? "ON" : "OFF")));
    }

    @Override
    public final void doCommand(CommandEvent event) { /* Intentionally empty */ }
}
