package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Oliver Johnson
 */
@Service
public class StatusCommand extends Command {
    private final BotSettingsManager settings;

    @Autowired
    public StatusCommand(BotSettingsManager settings) {
        this.settings = settings;
        this.name = "status";
        this.help = "shows the bots status";
        this.aliases = new String[]{"info"};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        MessageBuilder builder = new MessageBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                .setColor(new Color(244, 160, 0))
                .addField("Owner", (settings.get().getOwnerId() == 0)
                        ? "Owner is not set"
                        : Objects.requireNonNull(event.getGuild().getMemberById(settings.get().getOwnerId())).getUser().getAsTag(), true)
                .addField("Region", event.getGuild().getRegionRaw(), true)
                .addField("Channel Categories", Integer.toString(event.getGuild().getCategories().size()), true)
                .addField("Text Channels", Integer.toString(event.getGuild().getTextChannels().size()), true)
                .addField("Voice Channels", Integer.toString(event.getGuild().getVoiceChannels().size()), true)
                .addField("Members", Integer.toString(event.getGuild().getMembers().size()), true)
                .addField("Humans", Integer.toString((int) event.getGuild().getMembers()
                        .stream().filter(member -> !member.getUser().isBot()).count()), true)
                .addField("Bots", Integer.toString((int) event.getGuild().getMembers()
                        .stream().filter(member -> member.getUser().isBot()).count()), true)
                .addField("Online", Integer.toString((int) event.getGuild().getMembers()
                        .stream().filter(member -> member.getOnlineStatus() != OnlineStatus.OFFLINE).count()), true)
                .addField("Roles", Integer.toString(event.getGuild().getRoles().size()), false)
                .addField("Roles List",
                        Arrays.toString(event.getGuild().getRoles().stream().map(Role::getName).toArray()), false)
                .setFooter("ID: " + event.getSelfMember().getUser().getId() + " | Server Created • "
                        + event.getGuild().getTimeCreated(), event.getSelfMember().getUser().getAvatarUrl());
        event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }
}