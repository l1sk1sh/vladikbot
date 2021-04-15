package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.settings.Const;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class SetGameCommand extends OwnerCommand {
    private static final String gameNotSetMessage = "The game could not be set!";

    @Autowired
    public SetGameCommand() {
        this.name = "setgame";
        this.arguments = "<game>";
        this.help = "sets the game the bot is playing";
        this.guildOnly = false;
        this.children = new OwnerCommand[]{
                new SetListenCommand(),
                new SetStreamCommand(),
                new SetWatchCommand()
        };
    }

    @Override
    protected final void execute(CommandEvent event) {
        String title = event.getArgs().toLowerCase().startsWith(Const.StatusAction.playing.name())
                ? event.getArgs().substring(Const.StatusAction.playing.name().length()).trim()
                : event.getArgs();
        try {
            event.getJDA().getPresence().setActivity(title.isEmpty() ? null : Activity.playing(title));
            event.replySuccess(String.format("**%1$s** is %2$s.",
                    event.getSelfUser().getName(),
                    (title.isEmpty())
                            ? "no longer playing anything."
                            : "now playing `" + title + "`")
            );
        } catch (Exception e) {
            event.replyError(gameNotSetMessage);
        }
    }

    private final static class SetStreamCommand extends OwnerCommand {
        private SetStreamCommand() {
            this.name = "stream";
            this.aliases = new String[]{"twitch", Const.StatusAction.streaming.name()};
            this.help = "sets the game the bot is playing to a stream";
            this.arguments = "<username> <game>";
            this.guildOnly = false;
        }

        @Override
        protected final void execute(CommandEvent event) {
            String[] parts = event.getArgs().split("\\s+", 2);
            if (parts.length < 2) {
                event.replyError("Please include a twitch username and the name of the game to 'stream'.");
                return;
            }

            try {
                event.getJDA().getPresence().setActivity(Activity.streaming(parts[1], "https://twitch.tv/" + parts[0]));
                event.replySuccess(String.format("**%1$s** is now streaming `%2$s`.", event.getSelfUser().getName(), parts[1]));
            } catch (Exception e) {
                event.replyError(gameNotSetMessage);
            }
        }
    }

    private final static class SetListenCommand extends OwnerCommand {
        private SetListenCommand() {
            this.name = "listen";
            this.aliases = new String[]{Const.StatusAction.listening.name()};
            this.help = "sets the game the bot is listening to";
            this.arguments = "<title>";
            this.guildOnly = false;
        }

        @Override
        protected final void execute(CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.replyError("Please include a title to listen to!");
                return;
            }

            String title = event.getArgs().toLowerCase().startsWith("to")
                    ? event.getArgs().substring(2).trim()
                    : event.getArgs();
            try {
                event.getJDA().getPresence().setActivity(Activity.listening(title));
                event.replySuccess(String.format("**%1$s** is now listening to `%2$s`.", event.getSelfUser().getName(), title));
            } catch (Exception e) {
                event.replyError(gameNotSetMessage);
            }
        }
    }

    private final static class SetWatchCommand extends OwnerCommand {
        private SetWatchCommand() {
            this.name = "watch";
            this.aliases = new String[]{Const.StatusAction.watching.name()};
            this.help = "sets the game the bot is watching";
            this.arguments = "<title>";
            this.guildOnly = false;
        }

        @Override
        protected final void execute(CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.replyError("Please include a title to watch!");
                return;
            }

            String title = event.getArgs();
            try {
                event.getJDA().getPresence().setActivity(Activity.watching(title));
                event.replySuccess(String.format("**%1$s** is now watching to `%2$s`.", event.getSelfUser().getName(), title));
            } catch (Exception e) {
                event.replyError(gameNotSetMessage);
            }
        }
    }
}
