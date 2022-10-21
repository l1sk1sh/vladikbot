package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
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
public class SetNameCommand extends OwnerCommand {

    private static final String NAME_OPTION_KEY = "name";

    @Autowired
    public SetNameCommand() {
        this.name = "setname";
        this.help = "Sets the name of the bot";
        this.guildOnly = false;
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, NAME_OPTION_KEY, "New bot's name").setRequired(true));
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        OptionMapping nameOption = event.getOption(NAME_OPTION_KEY);
        if (nameOption == null) {
            event.replyFormat("%1$s Name is required for this command.", event.getClient().getWarning()).setEphemeral(true).queue();
            return;
        }

        String newName = nameOption.getAsString();

        try {
            String oldName = event.getJDA().getSelfUser().getName();
            event.getJDA().getSelfUser().getManager().setName(newName).complete(false);
            log.info("Name of bot was changed to {} by {}", nameOption, FormatUtils.formatAuthor(event));
            event.replyFormat("%1$s Name changed from `%2$s` to `%3$s`.", event.getClient().getSuccess(), oldName, newName).setEphemeral(true).queue();
        } catch (RateLimitedException e) {
            event.replyFormat("%1$s Name can only be changed twice per hour!", event.getClient().getError()).setEphemeral(true).queue();
        } catch (Exception e) {
            event.replyFormat("%1$s That name is not valid!", event.getClient().getError()).setEphemeral(true).queue();
        }
    }
}
