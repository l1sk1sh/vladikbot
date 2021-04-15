package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Optional;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class SettingsCommand extends Command {
    private final BotSettingsManager settings;
    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public SettingsCommand(BotSettingsManager settings, GuildSettingsRepository guildSettingsRepository) {
        this.settings = settings;
        this.name = "settings";
        this.help = "shows the bots settings";
        this.guildOnly = true;
        this.guildSettingsRepository = guildSettingsRepository;
    }

    @Override
    @SuppressWarnings("ConstantConditions") /* Suppressed as inspector can't detect null verification further in the code  */
    protected void execute(CommandEvent event) {
        Optional<GuildSettings> guildSettings = guildSettingsRepository.findById(event.getGuild().getIdLong());

        String defaultPlaylist = guildSettings.map(GuildSettings::getDefaultPlaylist).orElse(null);
        TextChannel textChannel = guildSettings.map(settings -> settings.getTextChannel(event.getGuild())).orElse(null);
        VoiceChannel voiceChannel = guildSettings.map(settings -> settings.getVoiceChannel(event.getGuild())).orElse(null);
        TextChannel notificationChannel = guildSettings.map(settings -> settings.getNotificationChannel(event.getGuild())).orElse(null);
        TextChannel newsChannel = guildSettings.map(settings -> settings.getNewsChannel(event.getGuild())).orElse(null);
        TextChannel memesChannel = guildSettings.map(settings -> settings.getMemesChannel(event.getGuild())).orElse(null);
        Role djRole = guildSettings.map(settings -> settings.getDjRole(event.getGuild())).orElse(null);

        MessageBuilder builder = new MessageBuilder()
                .append(Const.HEADPHONES_EMOJI + " **")
                .append(FormatUtils.filter(event.getSelfUser().getName()))
                .append("** settings:");

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(new Color(244, 160, 0))
                .setDescription(
                        "Text Channel: **"
                                + ((textChannel == null) ? "Any" : "#" + textChannel.getName()) + "**"
                                + "\r\nVoice Channel: **"
                                + ((voiceChannel == null) ? "Any" : voiceChannel.getName()) + "**"
                                + "\r\nNotification Channel: **"
                                + ((notificationChannel == null) ? "None" : notificationChannel.getName()) + "**"
                                + "\r\nNews Channel: **"
                                + ((newsChannel == null) ? "None" : newsChannel.getName()) + "**"
                                + "\r\nMemes Channel: **"
                                + ((memesChannel == null) ? "None" : memesChannel.getName()) + "**"
                                + "\r\nDJ Role: **"
                                + ((djRole == null) ? "None" : djRole.getName()) + "**"
                                + "\r\nRepeat Mode: **"
                                + (settings.get().isRepeat() ? "on" : "off") + "**"
                                + "\r\nDefault Playlist: **"
                                + ((defaultPlaylist == null) ? "None" : defaultPlaylist) + "**"
                                + "\r\nAuto Reply: **"
                                + (settings.get().isAutoReply() ? "on" : "off") + "**"
                                + "\r\nAuto Reply Matching Strategy: **"
                                + (settings.get().getMatchingStrategy()) + "**"
                                + "\r\nAuto Reply Chance: **"
                                + (settings.get().getReplyChance()) + "**"
                                + "\r\nActivity Simulation: **"
                                + (settings.get().isSimulateActivity() ? "on" : "off") + "**"
                                + "\r\nAuto text backup: **"
                                + (settings.get().isAutoTextBackup() ? "on" : "off") + "**"
                                + "\r\nAuto media backup: **"
                                + (settings.get().isAutoMediaBackup() ? "on" : "off") + "**"
                )
                .setFooter(event.getJDA().getGuilds().size() + " servers | "
                        + event.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inVoiceChannel()).count()
                        + " audio connections", null);
        event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }

}
