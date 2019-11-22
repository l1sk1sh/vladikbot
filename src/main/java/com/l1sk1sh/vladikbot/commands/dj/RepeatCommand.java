package com.l1sk1sh.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class RepeatCommand extends DJCommand {
    public RepeatCommand(Bot bot) {
        super(bot);
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
            value = !bot.getBotSettings().shouldRepeat();
        } else if (event.getArgs().equalsIgnoreCase("true") || event.getArgs().equalsIgnoreCase("on")) {
            value = true;
        } else if (event.getArgs().equalsIgnoreCase("false") || event.getArgs().equalsIgnoreCase("off")) {
            value = false;
        } else {
            event.replyError("Valid options are `on` or `off` (or leave empty to toggle).");
            return;
        }
        bot.getBotSettings().setRepeat(value);
        event.replySuccess(String.format("Repeat mode is now `%1$s`.", (value ? "ON" : "OFF")));
    }

    @Override
    public final void doCommand(CommandEvent event) { /* Intentionally empty */ }
}
