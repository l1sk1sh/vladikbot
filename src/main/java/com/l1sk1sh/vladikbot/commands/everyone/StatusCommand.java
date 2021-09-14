package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author l1sk1sh
 */
@Service
public class StatusCommand extends SlashCommand {
    private final BotSettingsManager settings;

    @Autowired
    public StatusCommand(BotSettingsManager settings) {
        this.settings = settings;
        this.name = "status";
        this.help = "Shows status of the current server";
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        MessageBuilder builder = new MessageBuilder();

        Guild currentGuild = event.getGuild();
        if (currentGuild == null) {
            event.reply("This command should not be called in DMs!").queue();

            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(currentGuild.getName(), null, currentGuild.getIconUrl())
                .setColor(new Color(244, 160, 0))
                .addField("Owner", (settings.get().getOwnerId() == 0)
                        ? "Owner is not set"
                        : Objects.requireNonNull(currentGuild.getMemberById(settings.get().getOwnerId())).getUser().getAsTag(), true)
                .addField("Channel Categories", Integer.toString(currentGuild.getCategories().size()), true)
                .addField("Text Channels", Integer.toString(currentGuild.getTextChannels().size()), true)
                .addField("Voice Channels", Integer.toString(currentGuild.getVoiceChannels().size()), true)
                .addField("Members", Integer.toString(currentGuild.getMembers().size()), true)
                .addField("Humans", Integer.toString((int) currentGuild.getMembers()
                        .stream().filter(member -> !member.getUser().isBot()).count()), true)
                .addField("Bots", Integer.toString((int) currentGuild.getMembers()
                        .stream().filter(member -> member.getUser().isBot()).count()), true)
                .addField("Online", Integer.toString((int) currentGuild.getMembers()
                        .stream().filter(member -> member.getOnlineStatus() != OnlineStatus.OFFLINE).count()), true)
                .addField("Roles", Integer.toString(currentGuild.getRoles().size()), false)
                .addField("Roles List",
                        Arrays.toString(currentGuild.getRoles().stream().map(Role::getName).toArray()), false)
                .setFooter("ID: " + event.getJDA().getSelfUser().getId() + " | Server Created • "
                        + currentGuild.getTimeCreated(), event.getJDA().getSelfUser().getAvatarUrl());
        event.reply(builder.setEmbeds(embedBuilder.build()).build()).queue();
    }
}