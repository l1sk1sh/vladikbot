package com.multiheaded.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.settings.Settings;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Role;

import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StatusCommand extends Command {
    private final Settings settings;

    public StatusCommand(Settings settings) {
        this.name = "status";
        this.help = "shows the bots status";
        this.aliases = new String[]{"info"};
        this.guildOnly = true;
        this.settings = settings;
    }

    @Override
    protected void execute(CommandEvent event) {
        MessageBuilder builder = new MessageBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                .setColor(new Color(244, 160, 0))
                .addField("Owner", event.getGuild().getMemberById(settings.getOwnerId()).getUser().getAsTag(), true)
                .addField("Region", event.getGuild().getRegionRaw(), true)
                .addField("Channel Categories", Integer.toString(event.getGuild().getCategories().size()), true)
                .addField("Text Channels", Integer.toString(event.getGuild().getTextChannels().size()), true)
                .addField("Voice Channels", Integer.toString(event.getGuild().getVoiceChannels().size()), true)
                .addField("Members", Integer.toString(event.getGuild().getMembers().size()), true)
                .addField("Humans", Integer.toString(event.getGuild().getMembers()
                        .stream().filter(member -> !member.getUser().isBot()).collect(Collectors.toList()).size()), true)
                .addField("Bots", Integer.toString(event.getGuild().getMembers()
                        .stream().filter(member -> member.getUser().isBot()).collect(Collectors.toList()).size()), true)
                .addField("Online", Integer.toString(event.getGuild().getMembers()
                        .stream().filter(member -> member.getOnlineStatus() != OnlineStatus.OFFLINE).collect(Collectors.toList()).size()), true)
                .addField("Roles", Integer.toString(event.getGuild().getRoles().size()), false)
                .addField("Roles List",
                        Arrays.toString(event.getGuild().getRoles().stream().map(Role::getName).toArray()), false)
                .setFooter("ID: " + event.getSelfMember().getUser().getId() + " | Server Created • "
                        + event.getGuild().getCreationTime(), event.getSelfMember().getUser().getAvatarUrl());
        event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }
}