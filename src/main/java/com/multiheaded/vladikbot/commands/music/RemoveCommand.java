package com.multiheaded.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.services.audio.AudioHandler;
import com.multiheaded.vladikbot.models.queue.QueuedTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class RemoveCommand extends MusicCommand {
    public RemoveCommand(VladikBot bot) {
        super(bot);
        this.name = "remove";
        this.help = "removes a song from the queue";
        this.arguments = "<position|ALL>";
        this.aliases = new String[]{"delete"};
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (audioHandler.getQueue().isEmpty()) {
            event.replyError("There is nothing in the queue!");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("all")) {
            int count = audioHandler.getQueue().removeAll(event.getAuthor().getIdLong());
            if (count == 0)
                event.replyWarning("You don't have any songs in the queue!");
            else
                event.replySuccess("Successfully removed your " + count + " entries.");
            return;
        }

        int pos;
        try {
            pos = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException e) {
            pos = 0;
        }

        if (pos < 1 || pos > audioHandler.getQueue().size()) {
            event.replyError("Position must be a valid integer between 1 and " + audioHandler.getQueue().size() + "!");
            return;
        }

        boolean isDJ = event.getMember().hasPermission(Permission.MANAGE_SERVER);
        if (!isDJ) {
            isDJ = event.getMember().getRoles().contains(bot.getSettings().getDjRole(event.getGuild()));
        }

        QueuedTrack queuedTrack = audioHandler.getQueue().get(pos - 1);
        if (queuedTrack.getIdentifier() == event.getAuthor().getIdLong()) {
            audioHandler.getQueue().remove(pos - 1);
            event.replySuccess("Removed **" + queuedTrack.getTrack().getInfo().title + "** from the queue");
        } else if (isDJ) {
            audioHandler.getQueue().remove(pos - 1);
            User user;
            try {
                user = event.getJDA().getUserById(queuedTrack.getIdentifier());
            } catch (Exception e) {
                user = null;
            }
            event.replySuccess("Removed **" + queuedTrack.getTrack().getInfo().title
                    + "** from the queue (requested by " + (user == null ? "someone" : "**" + user.getName() + "**") + ")");
        } else {
            event.replyError("You cannot remove **" + queuedTrack.getTrack().getInfo().title + "** because you didn't add it!");
        }
    }
}
