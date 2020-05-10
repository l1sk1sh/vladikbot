package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.settings.BotSettings;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.settings.GuildSpecificSettings;
import com.l1sk1sh.vladikbot.settings.GuildSpecificSettingsManager;
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
    private final GuildSpecificSettingsManager guildSpecificSettingsManager;
    private final static String on = "On";
    private final static String off = "Off";

    public SettingsCommand(BotSettings botSettings, GuildSpecificSettingsManager guildSpecificSettingsManager) {
        this.name = "settings";
        this.help = "shows the bots settings";
        this.guildOnly = true;
        this.botSettings = botSettings;
        this.guildSpecificSettingsManager = guildSpecificSettingsManager;
    }

    @Override
    @SuppressWarnings("ConstantConditions") /* Suppressed as inspector can't detect null verification further in the code  */
    protected void execute(CommandEvent event) {
        GuildSpecificSettings guildSpecificSettings = guildSpecificSettingsManager.getSettings(event.getGuild());
        MessageBuilder builder = new MessageBuilder()
                .append(Const.HEADPHONES_EMOJI + " **")
                .append(FormatUtils.filter(event.getSelfUser().getName()))
                .append("** settings:");
        TextChannel textChannel = guildSpecificSettings.getTextChannel(event.getGuild());
        TextChannel notificationChannel = guildSpecificSettings.getNotificationChannel(event.getGuild());
        VoiceChannel voiceChannel = guildSpecificSettings.getVoiceChannel(event.getGuild());
        Role djRole = guildSpecificSettings.getDjRole(event.getGuild());

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(new Color(244, 160, 0))
                .setDescription(
                        "Text Channel: **"
                                + ((textChannel == null) ? "Any" : "#" + textChannel.getName()) + "**"
                        + "\r\nVoice Channel: **"
                                + ((voiceChannel == null) ? "Any" : voiceChannel.getName()) + "**"
                        + "\r\nNotification Channel: **"
                                + ((notificationChannel == null) ? "Any" : notificationChannel.getName()) + "**"
                        + "\r\nDJ Role: **"
                                + ((djRole == null) ? "None" : djRole.getName()) + "**"
                        + "\r\nRepeat Mode: **"
                                + (botSettings.shouldRepeat() ? on : off) + "**"
                        + "\r\nDefault Playlist: **"
                                + ((guildSpecificSettings.getDefaultPlaylist() == null) ? "None" : guildSpecificSettings.getDefaultPlaylist()) + "**"
                        + "\r\nAuto Reply: **"
                                + (botSettings.shouldAutoReply() ? on : off) + "**"
                        + "\r\nAuto Reply Matching Strategy: **"
                                + (botSettings.getMatchingStrategy()) + "**"
                        + "\r\nAuto Reply Chance: **"
                                + (botSettings.getReplyChance()) + "**"
                        + "\r\nGame And Action Simulation: **"
                                + (botSettings.shouldSimulateActionsAndGamesActivity() ? on : off) + "**"
                        + "\r\nAuto text backup: **"
                                + (botSettings.shouldAutoTextBackup() ? on : off) + "**"
                        + "\r\nAuto media backup: **"
                                + (botSettings.shouldAutoMediaBackup() ? on : off) + "**"
                )
                .setFooter(event.getJDA().getGuilds().size() + " servers | "
                        + event.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inVoiceChannel()).count()
                        + " audio connections", null);
        event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }

}
