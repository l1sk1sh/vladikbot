package com.multiheaded.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.audio.AudioHandler;
import com.multiheaded.vladikbot.settings.Settings;
import com.multiheaded.vladikbot.settings.SettingsManager;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public abstract class MusicCommand extends Command {
    protected final VladikBot bot;
    protected boolean bePlaying;
    protected boolean beListening;

    protected MusicCommand(VladikBot bot) {
        this.bot = bot;
        this.guildOnly = true;
        this.category = new Category("Music");
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings settings = SettingsManager.getInstance().getSettings();
        TextChannel tchannel = settings.getTextChannel(event.getGuild());

        if (tchannel != null && !event.getTextChannel().equals(tchannel)) {
            try {
                event.getMessage().delete().queue();
            } catch (PermissionException ignore) {
            }
            event.replyInDm(event.getClient().getError()
                    + " You can only use that command in " + tchannel.getAsMention() + "!");
            return;
        }

        bot.getPlayerManager().setUpHandler(event.getGuild()); // no point constantly checking for this later
        if (bePlaying && !((AudioHandler) event.getGuild().getAudioManager().getSendingHandler())
                .isMusicPlaying(event.getJDA())) {
            event.reply(event.getClient().getError() + " There must be music playing to use that!");
            return;
        }
        if (beListening) {
            VoiceChannel current = event.getGuild().getSelfMember().getVoiceState().getChannel();
            if (current == null) {
                current = settings.getVoiceChannel(event.getGuild());
            }

            GuildVoiceState userState = event.getMember().getVoiceState();
            if (!userState.inVoiceChannel()
                    || userState.isDeafened()
                    || (current != null && !userState.getChannel().equals(current))) {
                event.replyError("You must be listening in "
                        + (current == null ? "a voice channel" : "**" + current.getName() + "**") + " to use that!");
                return;
            }
            if (!event.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
                try {
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                } catch (PermissionException ex) {
                    event.reply(event.getClient().getError()
                            + " I am unable to connect to **" + userState.getChannel().getName() + "**!");
                    return;
                }
            }
        }

        doCommand(event);
    }

    protected abstract void doCommand(CommandEvent event);
}
