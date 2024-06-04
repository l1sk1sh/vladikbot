package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author l1sk1sh
 */
@Service
public class PrivateMessageCommand extends AdminCommand {

    private static final String USER_OPTION = "user";
    private static final String MESSAGE_OPTION = "message";

    public PrivateMessageCommand() {
        this.name = "pm";
        this.help = "Send private message to specified user from this channel";
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.STRING, MESSAGE_OPTION, "Messages, that should be sent privately").setRequired(true));
        options.add(new OptionData(OptionType.USER, USER_OPTION, "User that should receive private message").setRequired(true));
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping messageOption = event.getOption(MESSAGE_OPTION);
        if (messageOption == null) {
            event.replyFormat("%1$s Message is required.",
                    event.getClient().getWarning()
            ).setEphemeral(true).queue();

            return;
        }

        OptionMapping userOption = event.getOption(USER_OPTION);
        if (userOption == null) {
            event.replyFormat("%1$s User is required.",
                    event.getClient().getWarning()
            ).setEphemeral(true).queue();

            return;
        }

        User user = userOption.getAsUser();
        user.openPrivateChannel().flatMap(channel -> channel.sendMessage(messageOption.getAsString())).queue();
        event.replyFormat("%1$s Message sent.", event.getClient().getSuccess()).setEphemeral(true).queue();
    }
}
