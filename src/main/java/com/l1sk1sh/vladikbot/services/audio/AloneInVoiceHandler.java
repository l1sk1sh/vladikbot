package com.l1sk1sh.vladikbot.services.audio;

import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - Null safety
 * - DI Spring
 * @author Michaili K
 */
@RequiredArgsConstructor
@Service
public class AloneInVoiceHandler {
    private final JDA jda;
    @Qualifier("backgroundThreadPool")
    private final ScheduledExecutorService backgroundThreadPool;
    private final BotSettingsManager settings;
    private final PlayerManager playerManager;
    private final Map<Long, Instant> aloneSince = new HashMap<>();
    private long aloneTimeUntilStop = 0;

    public void init() {
        aloneTimeUntilStop = settings.get().getAloneTimeUntilStop();
        if (aloneTimeUntilStop > 0) {
            backgroundThreadPool.scheduleWithFixedDelay(this::check, 0, 5, TimeUnit.SECONDS);
        }
    }

    private void check() {
        Set<Long> toRemove = new HashSet<>();
        for (Map.Entry<Long, Instant> entrySet : aloneSince.entrySet()) {
            if (entrySet.getValue().getEpochSecond() > Instant.now().getEpochSecond() - aloneTimeUntilStop) {
                continue;
            }

            Guild guild = jda.getGuildById(entrySet.getKey());

            if (guild == null) {
                toRemove.add(entrySet.getKey());
                continue;
            }

            ((AudioHandler) Objects.requireNonNull(guild.getAudioManager().getSendingHandler())).stopAndClear();
            guild.getAudioManager().closeAudioConnection();
            toRemove.add(entrySet.getKey());
        }
        toRemove.forEach(aloneSince::remove);
    }

    public void onVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (aloneTimeUntilStop <= 0) {
            return;
        }

        Guild guild = event.getEntity().getGuild();
        if (!playerManager.hasHandler(guild)) {
            return;
        }

        boolean alone = isAlone(guild);
        boolean inList = aloneSince.containsKey(guild.getIdLong());

        if (!alone && inList) {
            aloneSince.remove(guild.getIdLong());
        } else if (alone && !inList) {
            aloneSince.put(guild.getIdLong(), Instant.now());
        }
    }

    private boolean isAlone(Guild guild) {
        if (guild.getAudioManager().getConnectedChannel() == null) {
            return false;
        }
        return guild.getAudioManager().getConnectedChannel().getMembers().stream()
                .noneMatch(x ->
                        !Objects.requireNonNull(x.getVoiceState()).isDeafened()
                                && !x.getUser().isBot());
    }
}