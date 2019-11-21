package l1.multiheaded.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import l1.multiheaded.vladikbot.Bot;
import l1.multiheaded.vladikbot.services.audio.AudioHandler;
import net.dv8tion.jda.core.entities.User;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class ForceSkipCommand extends DJCommand {
    public ForceSkipCommand(Bot bot) {
        super(bot);
        this.name = "forceskip";
        this.aliases = new String[]{"modskip"};
        this.help = "skips the current song";
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        User user = event.getJDA().getUserById(audioHandler.getRequester());
        event.replySuccess(String.format("Skipped **%1$s** (requested by *%2$s*).",
                audioHandler.getPlayer().getPlayingTrack().getInfo().title,
                (user == null ? "someone" : user.getName())));
        audioHandler.getPlayer().stopTrack();
    }
}
