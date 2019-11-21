package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.settings.BotSettings;
import com.l1sk1sh.vladikbot.settings.Constants;
import com.l1sk1sh.vladikbot.settings.GuildSettings;
import com.l1sk1sh.vladikbot.settings.GuildSettingsManager;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.awt.*;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SettingsCommand extends Command {
    private final BotSettings botSettings;
    private final GuildSettingsManager guildSettingsManager;

    public SettingsCommand(BotSettings botSettings, GuildSettingsManager guildSettingsManager) {
        this.name = "settings";
        this.help = "shows the bots settings";
        this.guildOnly = true;
        this.botSettings = botSettings;
        this.guildSettingsManager = guildSettingsManager;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void execute(CommandEvent event) {
        GuildSettings guildSettings = (GuildSettings) guildSettingsManager.getSettings(event.getGuild());
        MessageBuilder builder = new MessageBuilder()
                .append(Constants.HEADPHONES_EMOJI + " **")
                .append(FormatUtils.filter(event.getSelfUser().getName()))
                .append("** settings:");
        TextChannel textChannel = guildSettings.getTextChannel(event.getGuild());
        TextChannel notificationChannel = guildSettings.getNotificationChannel(event.getGuild());
        VoiceChannel voiceChannel = guildSettings.getVoiceChannel(event.getGuild());
        Role djRole = guildSettings.getDjRole(event.getGuild());

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(new Color(244, 160, 0))
                .setDescription(
                        "Text Channel: "
                                + ((textChannel == null) ? "Any" : "**#" + textChannel.getName() + "**")
                        + "\r\nVoice Channel: "
                                + ((voiceChannel == null) ? "Any" : "**" + voiceChannel.getName() + "**")
                                + "\r\nNotification Channel: "
                                + ((notificationChannel == null) ? "Any" : "**" + notificationChannel.getName() + "**")
                        + "\r\nDJ Role: "
                                + ((djRole == null) ? "None" : "**" + djRole.getName() + "**")
                        + "\r\nRepeat Mode: **"
                                + (botSettings.shouldRepeat() ? "On" : "Off") + "**"
                        + "\r\nDefault Playlist: "
                                + ((guildSettings.getDefaultPlaylist() == null) ? "None" : "**" + guildSettings.getDefaultPlaylist() + "**")
                                + "\r\nAuto Moderation: **"
                                + (botSettings.isAutoModeration() ? "On" : "Off") + "**"
                                + "\r\nStatuses rotation: **"
                                + (botSettings.shouldRotateActionsAndGames() ? "On" : "Off") + "**"
                                + "\r\nText backup rotation: **"
                                + (botSettings.shouldRotateTextBackup() ? "On" : "Off") + "**"
                                + "\r\nMedia backup rotation: **"
                                + (botSettings.shouldRotateMediaBackup() ? "On" : "Off") + "**"
                )
                .setFooter(event.getJDA().getGuilds().size() + " servers | "
                        + event.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inVoiceChannel()).count()
                        + " audio connections", null);
        event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }

}
