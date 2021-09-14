package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

/**
 * @author l1sk1sh
 */
@Service
public class PingCommand extends SlashCommand {

    @Autowired
    public PingCommand() {
        this.name = "ping";
        this.help = "Checks the bot's latency";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Message message = event.getHook().retrieveOriginal().complete();
        event.getHook().editOriginal(
                String.format("Ping: %1$sms | Websocket: %2$sms",
                        message.getTimeCreated().until(message.getTimeCreated(), ChronoUnit.MILLIS),
                        event.getJDA().getGatewayPing())
        ).queue();
    }
}