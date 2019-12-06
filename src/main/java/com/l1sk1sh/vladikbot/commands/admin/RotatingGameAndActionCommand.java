package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.models.entities.GameAndAction;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Johnson
 */
public class RotatingGameAndActionCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(RotatingGameAndActionCommand.class);
    private final Bot bot;

    public RotatingGameAndActionCommand(Bot bot) {
        this.bot = bot;
        this.name = "simactivity";
        this.aliases = new String[]{"simactivity"};
        this.help = "Game and action rotation management (simulates activity of bot)";
        this.arguments = "<add|list|switch|delete>";
        this.guildOnly = false;
        this.children = new AdminCommand[]{
                new CreateCommand(),
                new ReadCommand(),
                new SwitchCommand(),
                new DeleteCommand()
        };
    }

    @Override
    protected final void execute(CommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString());
    }

    class CreateCommand extends AdminCommand {
        CreateCommand() {
            this.name = "create";
            this.aliases = new String[]{"make", "add"};
            this.help = "creates new *game and action* pair";
            this.arguments = "<action> <game>"; /* Used twisted to simplify UX (for ex. 'playing WoW', instead of 'WoW playing') */
            this.guildOnly = false;
        }

        @Override
        protected final void execute(CommandEvent event) {
            try {
                if (event.getArgs().isEmpty()) {
                    event.replyError("Please include pair *game* *action*!");
                    return;
                }

                Const.StatusAction action;
                if (event.getArgs().toLowerCase().startsWith(Const.StatusAction.playing.name())) {
                    action = Const.StatusAction.playing;
                } else if (event.getArgs().toLowerCase().startsWith(Const.StatusAction.listening.name())) {
                    action = Const.StatusAction.listening;
                } else if (event.getArgs().toLowerCase().startsWith(Const.StatusAction.watching.name())) {
                    action = Const.StatusAction.watching;
                } else {
                    event.replyWarning(String.format("Action word must be one of `[%1$s, %2$s, %3$s]`!",
                            Const.StatusAction.playing,
                            Const.StatusAction.listening,
                            Const.StatusAction.watching));
                    return;
                }

                GameAndAction newPair = new GameAndAction(
                        event.getArgs().substring(action.name().length()).trim(),
                        action);

                bot.getGameAndActionRotationManager().writeGameAndAction(newPair);
                log.info("Added new pair to rotation manager {}", newPair);
                event.replySuccess("New game and action pair was added.");
            } catch (IOException ioe) {
                log.error("IO error during addition of new rotation pair", ioe);
                event.replyError(String.format("Failed to write new game and action! `[%1$s]`", ioe.getLocalizedMessage()));
            }
        }
    }

    class ReadCommand extends AdminCommand {
        ReadCommand() {
            this.name = "all";
            this.aliases = new String[]{"available", "list", "read"};
            this.help = "lists all available rotation pairs";
            this.guildOnly = true;
        }

        @Override
        protected final void execute(CommandEvent event) {
            try {
                List<GameAndAction> pairs = bot.getGameAndActionRotationManager().getAllGamesAndActions();
                if (pairs == null) {
                    event.replyError("Failed to load available pairs!");
                } else if (pairs.isEmpty()) {
                    event.replyWarning("There are no records available!");
                } else {
                    String message = event.getClient().getSuccess() + " Acting pairs:\r\n";
                    StringBuilder builder = new StringBuilder(message);

                    builder.append("`Action`");
                    builder.append("  ");
                    builder.append("`Game`");

                    for (GameAndAction pair : pairs) {
                        if (builder.length() > 0) {
                            builder.append("\r\n");
                        }

                        builder.append(pair.getAction() != null ? URLEncoder.encode(pair.getAction().name(), "UTF-8") : "");
                        builder.append(" = ");
                        builder.append((pair.getGameName() != null ? URLEncoder.encode(pair.getGameName(), "UTF-8") : ""));
                    }

                    event.reply(builder.toString());
                }
            } catch (UnsupportedEncodingException e) {
                log.error("UTF-8 exception", e);
                event.replyError("Action requires UTF-8 encoding support!");
            } catch (IOException ioe) {
                log.error("IO exception during reading of rotation pair", ioe);
                event.replyError(String.format("Local folder couldn't be processed! `[%1$s]`", ioe.getLocalizedMessage()));
            }
        }
    }

    class SwitchCommand extends AdminCommand {
        SwitchCommand() {
            this.name = "switch";
            this.aliases = new String[]{"change"};
            this.help = "enables or disables simulation of bot's activity";
            this.arguments = "<on|off>";
            this.guildOnly = false;
        }

        @Override
        protected final void execute(CommandEvent event) {
            String[] args = event.getArgs().split("\\s+");
            if (args.length > 0) {
                for (String arg : args) {
                    switch (arg) {
                        case "on":
                        case "enable":
                            bot.getBotSettings().setRotateGameAndAction(true);
                            event.replySuccess("Rotation is now enabled!");
                            bot.getGameAndActionRotationManager().startRotation();
                            break;
                        case "off":
                        case "disable":
                            bot.getBotSettings().setRotateGameAndAction(false);
                            event.replySuccess("Rotation is now disabled!");
                            bot.getGameAndActionRotationManager().stopRotation();
                            break;
                    }
                }
            } else {
                event.replyWarning("Specify `on` or `off` argument for this command!");
            }
        }
    }

    class DeleteCommand extends AdminCommand {
        DeleteCommand() {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "deletes an existing rotation pair";
            this.arguments = "<game>";
            this.guildOnly = false;
        }

        @Override
        protected final void execute(CommandEvent event) {
            String gameName = event.getArgs().replaceAll("\\s+", "_");
            GameAndAction targetPair = bot.getGameAndActionRotationManager().getGameAndActionByGameName(gameName);

            if (targetPair == null) {
                event.replyError(String.format("Game `%1$s` doesn't exist!", gameName));
            } else {
                try {
                    bot.getGameAndActionRotationManager().deleteGameAndAction(targetPair);
                    log.info("Pair {} was removed by {}:[{}]", targetPair, event.getAuthor().getName(), event.getAuthor().getId());
                    event.replySuccess(String.format("Successfully deleted pair with game `%1$s`!", gameName));
                } catch (IOException ioe) {
                    log.error("IO error during removal of rotation pair", ioe);
                    event.replyError(String.format("Unable to delete this pair, that has game: `[%1$s]`.", ioe.getLocalizedMessage()));
                }
            }
        }
    }
}
