package com.l1sk1sh.vladikbot.services.audio;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.models.entities.Pair;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class NowPlayingHandler {
    private final Bot bot;
    private final Map<Long, Pair<Long, Long>> lastNP; /* guild -> channel, message */

    public NowPlayingHandler(Bot bot) {
        this.bot = bot;
        this.lastNP = new HashMap<>();
    }

    public void init() {
        if (!bot.getBotSettings().useNpImages()) {
            bot.getThreadPool().scheduleWithFixedDelay(this::updateAll, 0, 5, TimeUnit.SECONDS);
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

            Guild guild = bot.getJDA().getGuildById(guildId);
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
            Message message = audioHandler.getNowPlaying(bot.getJDA());
            if (message == null) {
                message = audioHandler.getNoMusicPlaying(bot.getJDA());
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
        Guild guild = bot.getJDA().getGuildById(guildId);
        if (guild == null) {
            return;
        }

        TextChannel textChannel = bot.getGuildSettings(guild).getTextChannel(guild);
        if (textChannel != null && guild.getSelfMember().hasPermission(textChannel, Permission.MANAGE_CHANNEL)) {
            String otherText;
            if (textChannel.getTopic() == null || textChannel.getTopic().isEmpty()) {
                otherText = "\u200B";
            } else if (textChannel.getTopic().contains("\u200B")) {
                otherText = textChannel.getTopic().substring(textChannel.getTopic().lastIndexOf("\u200B"));
            } else {
                otherText = "\u200B\r\n " + textChannel.getTopic();
            }

            String text = audioHandler.getTopicFormat(bot.getJDA()) + otherText;
            if (!text.equals(textChannel.getTopic())) {
                try {
                    if (wait) {
                        textChannel.getManager().setTopic(text).complete();
                    } else {
                        textChannel.getManager().setTopic(text).queue();
                    }
                } catch (PermissionException ignore) {
                }
            }
        }
    }

    /* "event"-based methods */
    void onTrackUpdate(long guildId, AudioTrack track, AudioHandler audioHandler) {
        /* Update bot status if applicable */
        if (bot.getBotSettings().shouldSongBeInStatus()) {
            if (track != null && bot.getJDA().getGuilds().stream()
                    .filter(g -> g.getSelfMember().getVoiceState().inVoiceChannel()).count() <= 1) {
                bot.getJDA().getPresence().setGame(Game.listening(track.getInfo().title));
            } else {
                bot.resetGame();
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
