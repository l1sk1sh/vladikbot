package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author l1sk1sh
 */
@Service
public class SayCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(SayCommand.class);

    private final JDA jda;

    @Autowired
    public SayCommand(JDA jda) {
        this.jda = jda;
        this.name = "say";
        this.help = "send message as a bot to specified channel id";
        this.arguments = "<channel_id> <message>";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a text channel or *this*.");
            return;
        }

        String[] args = event.getArgs().split(" ", 2);

        if (args.length < 2) {
            event.replyError("Please specify channel (or *this*) and message to send");
            return;
        }

        if (args[0].equalsIgnoreCase("this")) {
            event.reply(args[1]);
        } else {
            TextChannel textChannel = jda.getTextChannelById(args[0]);

            if (textChannel == null) {
                event.replyError(String.format("Unable to find text channel with id \"%1$s\".", args[0]));
                return;
            }

            textChannel.sendMessage(args[1]).queue();

            log.info("Bot sent message '{}' to channel '{}'. Sent by {}.", args[0], args[1], FormatUtils.formatAuthor(event));
        }
    }
}
