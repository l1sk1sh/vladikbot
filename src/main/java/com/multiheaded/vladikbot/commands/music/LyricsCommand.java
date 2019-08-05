package com.multiheaded.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jlyrics.LyricsClient;
import com.multiheaded.vladikbot.Bot;
import com.multiheaded.vladikbot.services.audio.AudioHandler;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class LyricsCommand extends MusicCommand {
    private final LyricsClient client = new LyricsClient();

    public LyricsCommand(Bot bot) {
        super(bot);
        this.name = "lyrics";
        this.help = "shows the lyrics to the currently-playing song";
        this.arguments = "<song name>";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        event.getChannel().sendTyping().queue();
        String title;
        if (event.getArgs().isEmpty()) {
            title = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler())
                    .getPlayer().getPlayingTrack().getInfo().title;
        } else {
            title = event.getArgs();
        }

        client.getLyrics(title).thenAccept(lyrics ->
        {
            if (lyrics == null) {
                event.replyError(String.format("Lyrics for `%1$s` could not be found!", title));
                return;
            }

            EmbedBuilder eb = new EmbedBuilder()
                    .setAuthor(lyrics.getAuthor())
                    .setColor(event.getSelfMember().getColor())
                    .setTitle(lyrics.getTitle(), lyrics.getURL());
            if (lyrics.getContent().length() > 15000) {
                event.replyWarning(String.format("Lyrics for `%1$s` found but likely not correct: %2$s.", title, lyrics.getURL()));
            } else if (lyrics.getContent().length() > 2000) {
                String content = lyrics.getContent().trim();
                while (content.length() > 2000) {
                    int index = content.lastIndexOf("\r\n\r\n", 2000);
                    if (index == -1) {
                        index = content.lastIndexOf("\r\n", 2000);
                    }
                    if (index == -1) {
                        index = content.lastIndexOf(" ", 2000);
                    }
                    if (index == -1) {
                        index = 2000;
                    }
                    event.reply(eb.setDescription(content.substring(0, index).trim()).build());
                    content = content.substring(index).trim();
                    eb.setAuthor(null).setTitle(null, null);
                }
                event.reply(eb.setDescription(content).build());
            } else {
                event.reply(eb.setDescription(lyrics.getContent()).build());
            }
        });
    }
}
