package com.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.multiheaded.vladikbot.models.SettingsFunction;
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
    private final SettingsFunction<Role> setDjRoleId;

    public SetDjCommand(SettingsFunction<Role> setDjRoleId) {
        this.name = "setdj";
        this.help = "sets the DJ role for certain music commands";
        this.arguments = "<rolename|none>";
        this.setDjRoleId = setDjRoleId;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a role name or *none*.");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            setDjRoleId.set(null);
            event.replySuccess("DJ role cleared.");
        } else {
            List<Role> list = FinderUtil.findRoles(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.replyWarning(String.format("No Roles found matching \"%1$s\".", event.getArgs()));
            } else if (list.size() > 1) {
                event.replyWarning(FormatUtils.listOfRoles(list, event.getArgs()));
            } else {
                setDjRoleId.set(list.get(0));
                event.replySuccess(String.format("DJ commands can now be used by users with the **%1$s** role.",
                        list.get(0).getName()));
            }
        }
    }

}
