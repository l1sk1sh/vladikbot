package com.l1sk1sh.vladikbot.services.audio;

import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.Pair;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.BotUtils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@RequiredArgsConstructor
@Service
public class NowPlayingHandler {
    private final JDA jda;
    @Qualifier("frontThreadPool")
    private final ScheduledExecutorService frontThreadPool;
    private final BotSettingsManager settings;
    private final GuildSettingsRepository guildSettingsRepository;
    private final Map<Long, Pair<Long, Long>> lastNP; /* guild -> channel, message */

    public void init() {
        if (!settings.get().isNpImages()) {
            frontThreadPool.scheduleWithFixedDelay(this::updateAll, 0, 5, TimeUnit.SECONDS);
        }
    }

    public void setLastNPMessage(Message message) {
        lastNP.put(message.getGuild().getIdLong(), new Pair<>(message.getTextChannel().getIdLong(), message.getIdLong()));
    }

    public void clearLastNPMessage(Guild guild) {
        lastNP.remove(guild.getIdLong());
    }

    private void updateAll() {
        /* Might be subject to bug connected with music playing. Review commit when this message was added */
        Set<Long> toRemove = new HashSet<>();

        for (Map.Entry<Long, Pair<Long, Long>> entry : lastNP.entrySet()) {
            long guildId = entry.getKey();

            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                toRemove.add(guildId);
                continue;
            }

            Pair<Long, Long> pair = entry.getValue();
            TextChannel textChannel = guild.getTextChannelById(pair.getKey());
            if (textChannel == null) {
                toRemove.add(guildId);
                continue;
            }

            AudioHandler audioHandler = (AudioHandler) guild.getAudioManager().getSendingHandler();
            if (audioHandler == null) {
                continue;
            }

            Message message = audioHandler.getNowPlaying(jda);
            if (message == null) {
                message = audioHandler.getNoMusicPlaying(jda);
                toRemove.add(guildId);
            }

            try {
                textChannel.editMessageById(pair.getValue(), message).queue(m -> {
                }, t -> lastNP.remove(guildId));
            } catch (Exception e) {
                toRemove.add(guildId);
            }
        }

        toRemove.forEach(lastNP::remove);
    }

    public void updateTopic(long guildId, AudioHandler audioHandler, boolean wait) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            return;
        }

        Optional<GuildSettings> settings = guildSettingsRepository.findById(guild.getIdLong());
        TextChannel textChannel = settings.map(guildSettings -> guildSettings.getTextChannel(guild)).orElse(null);

        if (textChannel != null && guild.getSelfMember().hasPermission(textChannel, Permission.MANAGE_CHANNEL)) {
            String otherText;
            String topic = textChannel.getTopic();
            if (topic == null || topic.isEmpty()) {
                otherText = "\u200B";
            } else if (topic.contains("\u200B")) {
                otherText = topic.substring(topic.lastIndexOf("\u200B"));
            } else {
                otherText = "\u200B\r\n " + topic;
            }

            String text = audioHandler.getTopicFormat(jda) + otherText;
            if (!text.equals(topic)) {
                try {
                    /*
                     * Normally here if 'wait' was false, we'd want to queue, however,
                     * new discord ratelimits specifically limiting changing channel topics
                     * mean we don't want a backlog of changes piling up, so if we hit a
                     * ratelimit, we just won't change the topic this time
                     */
                    textChannel.getManager().setTopic(text).complete(wait);
                } catch (PermissionException | RateLimitedException ignored) {
                }
            }
        }
    }

    /* "event"-based methods */
    void onTrackUpdate(long guildId, AudioTrack track, AudioHandler audioHandler) {

        /* Update bot status if applicable */
        if (settings.get().isSongInStatus()) {

            if (track != null && jda.getGuilds().stream()
                    .filter(g -> Objects.requireNonNull(g.getSelfMember().getVoiceState()).inVoiceChannel()).count() <= 1) {
                jda.getPresence().setActivity(Activity.listening(track.getInfo().title));
            } else {
                BotUtils.resetActivity(settings.get(), jda);
            }
        }

        /* Update channel topic if applicable */
        updateTopic(guildId, audioHandler, false);
    }

    public void onMessageDelete(Guild guild, long messageId) {
        Pair<Long, Long> pair = lastNP.get(guild.getIdLong());
        if (pair == null) {
            return;
        }
        if (pair.getValue() == messageId) {
            lastNP.remove(guild.getIdLong());
        }
    }
}
