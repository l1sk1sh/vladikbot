package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.Objects;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public abstract class MusicCommand extends Command {
    protected final Bot bot;
    protected boolean bePlaying;
    protected boolean beListening;

    protected MusicCommand(Bot bot) {
        this.bot = bot;
        this.guildOnly = true;
        this.category = new Category("Music");
    }

    @Override
    protected void execute(CommandEvent event) {
        TextChannel textChannel = bot.getGuildSettings(event.getGuild()).getTextChannel(event.getGuild());

        if (textChannel != null && !event.getTextChannel().equals(textChannel)) {
            try {
                event.getMessage().delete().queue();
            } catch (PermissionException ignore) { /* Ignore */ }
            event.replyInDm(String.format("%1$s You can only use that command in %2$s!",
                    event.getClient().getError(), textChannel.getAsMention()));
            return;
        }

        bot.getPlayerManager().setUpHandler(event.getGuild()); /* No point in constantly checking for this later */
        if (bePlaying && !((AudioHandler) Objects.requireNonNull(event.getGuild().getAudioManager().getSendingHandler()))
                .isMusicPlaying(event.getJDA())) {
            event.replyError("There must be music playing to use that!");
            return;
        }

        if (beListening) {
            VoiceChannel current = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel();
            if (current == null) {
                current = bot.getGuildSettings(event.getGuild()).getVoiceChannel(event.getGuild());
            }

            GuildVoiceState userState = event.getMember().getVoiceState();
            if (!Objects.requireNonNull(userState).inVoiceChannel()
                    || userState.isDeafened()
                    || (current != null && !Objects.requireNonNull(userState.getChannel()).equals(current))) {
                event.replyError(String.format("You must be listening in *%1$s* to use that!",
                        (current == null ? "a voice channel" : current.getName())));
                return;
            }

            VoiceChannel afkChannel = userState.getGuild().getAfkChannel();
            if (afkChannel != null && afkChannel.equals(userState.getChannel())) {
                event.replyError("You cannot use that command in an AFK channel!");
                return;
            }

            if (!event.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
                try {
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                } catch (PermissionException ex) {
                    event.replyError(String.format("I am unable to connect to **%1$s**!", Objects.requireNonNull(userState.getChannel()).getName()));
                    return;
                }
            }
        }

        doCommand(event);
    }

    protected abstract void doCommand(CommandEvent event);
}
