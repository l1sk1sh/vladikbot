package com.l1sk1sh.vladikbot.commands.owner;

import com.l1sk1sh.vladikbot.utils.CommandUtils;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * - Separated commands
 * @author John Grosh
 */
@Service
public class SetPresenceCommand extends OwnerCommand {

    @Autowired
    public SetPresenceCommand() {
        this.name = "setpresence";
        this.help = "Sets the presence of the bot";
        this.guildOnly = false;
        this.children = new OwnerCommand[]{
                new PlayCommand(),
                new ListenCommand(),
                new StreamCommand(),
                new WatchCommand()
        };
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(this, children, name).toString()).setEphemeral(true).queue();
    }

    private static final class PlayCommand extends OwnerCommand {

        private static final String GAME_OPTION_KEY = "game";

        private PlayCommand() {
            this.name = "play";
            this.help = "sets the game the bot is playing to a stream";
            this.guildOnly = false;
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, GAME_OPTION_KEY, "Game bot is playing").setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            OptionMapping gameOption = event.getOption(GAME_OPTION_KEY);
            if (gameOption == null) {
                event.replyFormat("%1$s Game is required for this command.", getClient().getWarning()).setEphemeral(true).queue();
                return;
            }

            String game = gameOption.getAsString();

            try {
                event.getJDA().getPresence().setActivity(game.isEmpty() ? null : Activity.playing(game));
                event.replyFormat("%1$s **%2$s** is %3$s.",
                        getClient().getSuccess(),
                        event.getJDA().getSelfUser().getName(),
                        (game.isEmpty())
                                ? "no longer playing anything."
                                : "now playing `" + game + "`"
                ).setEphemeral(true).queue();
            } catch (Exception e) {
                event.replyFormat("%1$s Failed to set playing presence.", getClient().getError()).setEphemeral(true).queue();
            }
        }
    }

    private static final class StreamCommand extends OwnerCommand {

        private static final String STREAM_OPTION_KEY = "stream";
        private static final String USER_OPTION_KEY = "user";

        private StreamCommand() {
            this.name = "stream";
            this.help = "Sets the stream and the user the bot streaming";
            this.guildOnly = false;
            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, STREAM_OPTION_KEY, "Name of the stream").setRequired(true));
            options.add(new OptionData(OptionType.STRING, USER_OPTION_KEY, "Twitch user").setRequired(true));
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            OptionMapping userOption = event.getOption(USER_OPTION_KEY);
            if (userOption == null) {
                event.replyFormat("%1$s Specify user that streams!",
                        getClient().getWarning()
                ).setEphemeral(true).queue();

                return;
            }

            OptionMapping streamOption = event.getOption(STREAM_OPTION_KEY);
            if (streamOption == null) {
                event.replyFormat("%1$s Specify game that is being streamed!",
                        getClient().getWarning()
                ).setEphemeral(true).queue();

                return;
            }

            try {
                event.getJDA().getPresence().setActivity(Activity.streaming(streamOption.getAsString(), "https://twitch.tv/" + userOption.getAsString()));
                event.replyFormat("%1$s **%2$s** is now streaming `%3$s`.", event.getJDA().getSelfUser().getName(), streamOption.getAsString()).setEphemeral(true).queue();
            } catch (Exception e) {
                event.replyFormat("%1$s Failed to set streaming presence.", getClient().getError()).setEphemeral(true).queue();
            }
        }
    }

    private static final class ListenCommand extends OwnerCommand {

        private static final String TITLE_OPTION_KEY = "title";

        private ListenCommand() {
            this.name = "listen";
            this.help = "Sets the music the bot is listening to";
            this.guildOnly = false;
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, TITLE_OPTION_KEY, "Title bot is listening").setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            OptionMapping titleOption = event.getOption(TITLE_OPTION_KEY);
            if (titleOption == null) {
                event.replyFormat("%1$s Title is required for this command.", getClient().getWarning()).setEphemeral(true).queue();
                return;
            }

            String title = titleOption.getAsString();
            try {
                event.getJDA().getPresence().setActivity(Activity.listening(title));
                event.replyFormat("%1$s **%2$s** is now listening to `%3$s`.", getClient().getSuccess(), event.getJDA().getSelfUser().getName(), title).setEphemeral(true).queue();
            } catch (Exception e) {
                event.replyFormat("%1$s Failed to set listening presence.", getClient().getError()).setEphemeral(true).queue();
            }
        }
    }

    private static final class WatchCommand extends OwnerCommand {

        private static final String TITLE_OPTION_KEY = "title";

        private WatchCommand() {
            this.name = "watch";
            this.help = "Sets the title the bot is watching";
            this.guildOnly = false;
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, TITLE_OPTION_KEY, "Title bot is watching").setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            OptionMapping titleOption = event.getOption(TITLE_OPTION_KEY);
            if (titleOption == null) {
                event.replyFormat("%1$s Title is required for this command.", getClient().getWarning()).setEphemeral(true).queue();
                return;
            }

            String title = titleOption.getAsString();
            try {
                event.getJDA().getPresence().setActivity(Activity.watching(title));
                event.replyFormat("%1$s **%2$s** is now watching to `%3$s`.", getClient().getSuccess(), event.getJDA().getSelfUser().getName(), title).setEphemeral(true).queue();
            } catch (Exception e) {
                event.replyFormat("%1$s Failed to set watching presence.", getClient().getError()).setEphemeral(true).queue();
            }
        }
    }
}
