package com.multiheaded.disbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.Bot;
import com.multiheaded.disbot.audio.AudioHandler;
import net.dv8tion.jda.core.entities.User;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SkipCommand extends MusicCommand {
    public SkipCommand(Bot bot) {
        super(bot);
        this.name = "skip";
        this.help = "votes to skip the current song";
        this.aliases = new String[]{"voteskip"};
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (event.getAuthor().getIdLong() == handler.getRequester()) {
            event.reply(event.getClient().getSuccess() + " Skipped **"
                    + handler.getPlayer().getPlayingTrack().getInfo().title + "**");
            handler.getPlayer().stopTrack();
        } else {
            int listeners = (int) event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();
            String msg;
            if (handler.getVotes().contains(event.getAuthor().getId()))
                msg = event.getClient().getWarning() + " You already voted to skip this song `[";
            else {
                msg = event.getClient().getSuccess() + " You voted to skip the song `[";
                handler.getVotes().add(event.getAuthor().getId());
            }
            int skippers = (int) event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> handler.getVotes().contains(m.getUser().getId())).count();
            int required = (int) Math.ceil(listeners * .55);
            msg += skippers + " votes, " + required + "/" + listeners + " needed]`";
            if (skippers >= required) {
                User u = event.getJDA().getUserById(handler.getRequester());
                msg += "\n" + event.getClient().getSuccess() + " Skipped **"
                        + handler.getPlayer().getPlayingTrack().getInfo().title
                        + "**" + (handler.getRequester() == 0 ?
                        "" : " (requested by " + (u == null ? "someone" : "**" + u.getName() + "**") + ")");
                handler.getPlayer().stopTrack();
            }
            event.reply(msg);
        }
    }

}
