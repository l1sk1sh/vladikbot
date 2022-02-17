package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Slf4j
@Service
public class SetDjCommand extends AdminCommand {

    private static final String ROLE_ID_OPTION_KEY = "role_id";

    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public SetDjCommand(GuildSettingsRepository guildSettingsRepository) {
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "setdj";
        this.help = "Sets the DJ role for advanced music commands";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, ROLE_ID_OPTION_KEY, "ID of a new DJ. Set 'none' to remove one").setRequired(false));
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());
        Role currentDJRole = settings.map(guildSettings -> guildSettings.getDjRole(event.getGuild())).orElse(null);

        OptionMapping roleOption = event.getOption(ROLE_ID_OPTION_KEY);
        if (roleOption == null) {
            event.replyFormat("Current DJ role is `%1$s`", (currentDJRole == null) ? "nobody" : currentDJRole.getAsMention()).setEphemeral(true).queue();
            return;
        }

        String newDJRoleId = roleOption.getAsString();
        if (newDJRoleId.equals("0") || newDJRoleId.equalsIgnoreCase("none")) {
            guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                setting.setDjRoleId(0L);
                guildSettingsRepository.save(setting);
                log.info("DJ role cleared. Cleared by {}.", FormatUtils.formatAuthor(event));
                event.replyFormat("%1$s DJ role cleared.", getClient().getSuccess()).setEphemeral(true).queue();
            });
        } else {
            List<Role> list = FinderUtil.findRoles(newDJRoleId, event.getGuild());
            if (list.isEmpty()) {
                event.replyFormat("%1$s No Roles found matching \"%2$s\".", getClient().getWarning(), newDJRoleId).setEphemeral(true).queue();
            } else {
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                    setting.setDjRoleId(list.get(0).getIdLong());
                    guildSettingsRepository.save(setting);
                    log.info("DJ role now available for {}. Set by {}.", list.get(0).getName(), FormatUtils.formatAuthor(event));
                    event.replyFormat("%1$s DJ commands can now be used by users with the **%2$s** role.", getClient().getSuccess(),
                            list.get(0).getName()).setEphemeral(true).queue();
                });
            }
        }
    }
}
