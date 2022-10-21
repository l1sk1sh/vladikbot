package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Slf4j
@Service
public class SetStatusCommand extends OwnerCommand {

    private static final String STATUS_OPTION_KEY = "status";

    @Autowired
    public SetStatusCommand() {
        this.name = "setstatus";
        this.help = "Sets the status the bot displays";
        this.guildOnly = false;
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, STATUS_OPTION_KEY, "New bot's status").setRequired(true));
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        OptionMapping statusOption = event.getOption(STATUS_OPTION_KEY);
        if (statusOption == null) {
            event.replyFormat("%1$s Status is required for this command.", event.getClient().getWarning()).setEphemeral(true).queue();
            return;
        }

        try {
            OnlineStatus status = OnlineStatus.fromKey(statusOption.getAsString());
            if (status == OnlineStatus.UNKNOWN) {
                event.replyFormat("%1$s Please include one of the following statuses: `ONLINE`, `IDLE`, `DND`, `INVISIBLE`", event.getClient().getWarning()).setEphemeral(true).queue();
            } else {
                event.getJDA().getPresence().setStatus(status);
                log.info("Status of bot was changed to {} by {}", status.getKey().toUpperCase(), FormatUtils.formatAuthor(event));
                event.replyFormat("%1$s Set the status to `%2$s`", event.getClient().getSuccess(), status.getKey().toUpperCase()).setEphemeral(true).queue();
            }
        } catch (Exception e) {
            event.replyFormat("%1$s The status could not be set!", event.getClient().getError()).setEphemeral(true).queue();
        }
    }
}
