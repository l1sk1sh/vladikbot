package com.l1sk1sh.vladikbot.services.audio;

import com.l1sk1sh.vladikbot.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * - Null safety
 * @author Michaili K
 */
public class AloneInVoiceHandler {
    private final Bot bot;
    private final HashMap<Guild, Instant> aloneSince = new HashMap<>();
    private long aloneTimeUntilStop = 0;

    public AloneInVoiceHandler(Bot bot) {
        this.bot = bot;
    }

    public void init() {
        aloneTimeUntilStop = bot.getBotSettings().getAloneTimeUntilStop();
        if (aloneTimeUntilStop > 0) {
            bot.getBackThreadPool().scheduleWithFixedDelay(this::check, 0, 5, TimeUnit.SECONDS);
        }
    }

    private void check() {
        Set<Guild> toRemove = new HashSet<>();
        for (Map.Entry<Guild, Instant> entrySet : aloneSince.entrySet()) {
            if (entrySet.getValue().getEpochSecond() > Instant.now().getEpochSecond() - aloneTimeUntilStop) {
                continue;
            }

            ((AudioHandler) Objects.requireNonNull(entrySet.getKey().getAudioManager().getSendingHandler())).stopAndClear();
            entrySet.getKey().getAudioManager().closeAudioConnection();

            toRemove.add(entrySet.getKey());
        }
        toRemove.forEach(aloneSince::remove);
    }

    public void onVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (aloneTimeUntilStop <= 0) {
            return;
        }

        Guild guild = event.getEntity().getGuild();
        if (!bot.getPlayerManager().hasHandler(guild)) {
            return;
        }

        boolean alone = isAlone(guild);
        boolean inList = aloneSince.containsKey(guild);

        if (!alone && inList) {
            aloneSince.remove(guild);
        } else if (alone && !inList) {
            aloneSince.put(guild, Instant.now());
        }
    }

    private boolean isAlone(Guild guild) {
        if (guild.getAudioManager().getConnectedChannel() == null) {
            return false;
        }
        return guild.getAudioManager().getConnectedChannel().getMembers().stream()
                .noneMatch(x ->
                        !Objects.requireNonNull(x.getVoiceState()).isDeafened()
                                && x.getIdLong() != bot.getJDA().getSelfUser().getIdLong());
    }
}