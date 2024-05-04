package com.l1sk1sh.vladikbot.services.audio;

import com.l1sk1sh.vladikbot.VladikBot;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.Pair;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    @Qualifier("frontThreadPool")
    private final ScheduledExecutorService frontThreadPool;
    private final BotSettingsManager settings;
    private final Map<Long, Pair<Long, Long>> lastNP; /* guild -> channel, message */

    public void init() {
        if (!settings.get().isNpImages()) {
            frontThreadPool.scheduleWithFixedDelay(this::updateAll, 0, 5, TimeUnit.SECONDS);
        }
    }

    public void setLastNPMessage(Message message) {
        lastNP.put(message.getGuild().getIdLong(), new Pair<>(message.getChannel().asTextChannel().getIdLong(), message.getIdLong()));
    }

    public void clearLastNPMessage(Guild guild) {
        lastNP.remove(guild.getIdLong());
    }

    private void updateAll() {
        /* Might be subject to bug connected with music playing. Review commit when this message was added */
        Set<Long> toRemove = new HashSet<>();
        JDA jda = VladikBot.jda();

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

            MessageCreateData message = audioHandler.getNowPlaying(jda);
            if (message == null) {
                message = audioHandler.getNoMusicPlaying(jda);
                toRemove.add(guildId);
            }

            try {
                textChannel.editMessageById(pair.getValue(), message.getContent()).queue(m -> {
                }, t -> lastNP.remove(guildId));
            } catch (Exception e) {
                toRemove.add(guildId);
            }
        }

        toRemove.forEach(lastNP::remove);
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
