package com.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.multiheaded.vladikbot.settings.Settings;
import com.multiheaded.vladikbot.settings.SettingsManager;
import com.multiheaded.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.core.entities.Role;

import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SetDjCommand extends AdminCommand {
    public SetDjCommand() {
        this.name = "setdj";
        this.help = "sets the DJ role for certain music commands";
        this.arguments = "<rolename|NONE>";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " Please include a role name or NONE");
            return;
        }

        Settings settings = SettingsManager.getInstance().getSettings();
        if (event.getArgs().equalsIgnoreCase("none")) {
            settings.setDjRoleId(null);
            event.reply(event.getClient().getSuccess() + " DJ role cleared; Only Admins can use the DJ commands.");
        } else {
            List<Role> list = FinderUtil.findRoles(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.reply(event.getClient().getWarning() + " No Roles found matching \"" + event.getArgs() + "\"");
            } else if (list.size() > 1) {
                event.reply(event.getClient().getWarning() + FormatUtils.listOfRoles(list, event.getArgs()));
            } else {
                settings.setDjRoleId(list.get(0));
                event.reply(event.getClient().getSuccess()
                        + " DJ commands can now be used by users with the **" + list.get(0).getName() + "** role.");
            }
        }
    }

}
