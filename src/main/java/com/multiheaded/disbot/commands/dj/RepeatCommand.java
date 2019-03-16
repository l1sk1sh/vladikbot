package com.multiheaded.disbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.VladikBot;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class RepeatCommand extends DJCommand {
    public RepeatCommand(VladikBot bot) {
        super(bot);
        this.name = "repeat";
        this.help = "re-adds music to the queue when finished";
        this.arguments = "[on|off]";
        this.guildOnly = true;
    }

    // override musiccommand's execute because we don't actually care where this is used
    @Override
    protected void execute(CommandEvent event) {
        boolean value;
        Settings settings = SettingsManager.getInstance().getSettings();
        if (event.getArgs().isEmpty()) {
            value = !settings.shouldRepeat();
        } else if (event.getArgs().equalsIgnoreCase("true") || event.getArgs().equalsIgnoreCase("on")) {
            value = true;
        } else if (event.getArgs().equalsIgnoreCase("false") || event.getArgs().equalsIgnoreCase("off")) {
            value = false;
        } else {
            event.replyError("Valid options are `on` or `off` (or leave empty to toggle)");
            return;
        }
        settings.setRepeat(value);
        event.replySuccess("Repeat mode is now `" + (value ? "ON" : "OFF") + "`");
    }

    @Override
    public void doCommand(CommandEvent event) { /* Intentionally Empty */ }
}
