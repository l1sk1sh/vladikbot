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
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (event.getAuthor().getIdLong() == audioHandler.getRequester()) {
            event.reply(event.getClient().getSuccess() + " Skipped **"
                    + audioHandler.getPlayer().getPlayingTrack().getInfo().title + "**");
            audioHandler.getPlayer().stopTrack();
        } else {
            int listeners = (int) event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(member -> !member.getUser().isBot() && !member.getVoiceState().isDeafened()).count();
            String message;

            if (audioHandler.getVotes().contains(event.getAuthor().getId())) {
                message = event.getClient().getWarning() + " You already voted to skip this song `[";
            } else {
                message = event.getClient().getSuccess() + " You voted to skip the song `[";
                audioHandler.getVotes().add(event.getAuthor().getId());
            }

            int skippers = (int) event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> audioHandler.getVotes().contains(m.getUser().getId())).count();
            int required = (int) Math.ceil(listeners * .55);
            message += skippers + " votes, " + required + "/" + listeners + " needed]`";

            if (skippers >= required) {
                User user = event.getJDA().getUserById(audioHandler.getRequester());
                message += "\n" + event.getClient().getSuccess() + " Skipped **"
                        + audioHandler.getPlayer().getPlayingTrack().getInfo().title
                        + "**" + (audioHandler.getRequester() == 0 ?
                        "" : " (requested by " + (user == null ? "someone" : "**" + user.getName() + "**") + ")");
                audioHandler.getPlayer().stopTrack();
            }
            event.reply(message);
        }
    }

}
