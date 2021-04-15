package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class SetDjCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(SetDjCommand.class);

    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public SetDjCommand(GuildSettingsRepository guildSettingsRepository) {
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "setdj";
        this.help = "sets the DJ role for certain music commands";
        this.arguments = "<rolename|none>";
    }

    @Override
    protected final void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a role name or *none*.");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                setting.setDjRoleId(0L);
                guildSettingsRepository.save(setting);
                log.info("DJ role cleared. Cleared by {}.", FormatUtils.formatAuthor(event));
                event.replySuccess("DJ role cleared.");
            });
        } else {
            List<Role> list = FinderUtil.findRoles(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.replyWarning(String.format("No Roles found matching \"%1$s\".", event.getArgs()));
            } else if (list.size() > 1) {
                event.replyWarning(FormatUtils.listOfRoles(list, event.getArgs()));
            } else {
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                    setting.setDjRoleId(list.get(0).getIdLong());
                    guildSettingsRepository.save(setting);
                    log.info("DJ role now available for {}. Set by {}.", list.get(0).getName(), FormatUtils.formatAuthor(event));
                    event.replySuccess(String.format("DJ commands can now be used by users with the **%1$s** role.",
                            list.get(0).getName()));
                });
            }
        }
    }
}
