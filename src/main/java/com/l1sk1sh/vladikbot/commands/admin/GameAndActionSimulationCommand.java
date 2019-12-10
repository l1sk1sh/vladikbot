package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.models.entities.GameAndAction;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class GameAndActionSimulationCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(GameAndActionSimulationCommand.class);
    private final Bot bot;

    public GameAndActionSimulationCommand(Bot bot) {
        this.bot = bot;
        this.name = "simactivity";
        this.aliases = new String[]{"simactivity"};
        this.help = "Game and action simulation management (simulates activity of bot)";
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
            this.help = "creates new *game and action* rule";
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
                        event.getArgs().substring(action.name().length()).trim(), action);

                bot.getGameAndActionSimulationManager().writeRule(newPair);
                log.info("Added new rule to GAASimulation {}", newPair);
                event.replySuccess("New rule was added.");
            } catch (IOException ioe) {
                log.error("IO error during addition of new GAASimulation rule", ioe);
                event.replyError(String.format("Failed to write new game and action rule! `[%1$s]`", ioe.getLocalizedMessage()));
            }
        }
    }

    class ReadCommand extends AdminCommand {
        ReadCommand() {
            this.name = "all";
            this.aliases = new String[]{"available", "list", "read"};
            this.help = "lists all available rules";
            this.guildOnly = true;
        }

        @Override
        protected final void execute(CommandEvent event) {
            try {
                List<GameAndAction> list = bot.getGameAndActionSimulationManager().getAllRules();
                if (list == null) {
                    event.replyError("Failed to load available rules!");
                } else if (list.isEmpty()) {
                    event.replyWarning("There are no records available!");
                } else {
                    String message = event.getClient().getSuccess() + " Acting rules:\r\n";
                    StringBuilder builder = new StringBuilder(message);
                    list.forEach(rule -> builder.append("`").append(rule.getAction()).append(" ").append(rule.getGameName()).append("`").append("\r\n"));
                    event.reply(builder.toString());
                }
            } catch (IOException ioe) {
                log.error("IO exception during reading of GAASimulation rules", ioe);
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
                            try {
                                bot.getBotSettings().setSimulateActionAndGameActivity(true);
                                event.replySuccess("Simulation of bot's activity is now enabled!");
                                bot.getGameAndActionSimulationManager().enableSimulation();
                                break;
                            } catch (IOException ioe) {
                                log.error("Failed to enable GAASimulation", ioe);
                                event.replyError(String.format("Failed to enable Game and Activity Simulation! `[%1$s]`", ioe.getLocalizedMessage()));
                                bot.getBotSettings().setSimulateActionAndGameActivity(false);
                            }
                        case "off":
                        case "disable":
                            bot.getBotSettings().setSimulateActionAndGameActivity(false);
                            event.replySuccess("Simulation of bot's activity is now disabled!");
                            bot.getGameAndActionSimulationManager().disableSimulation();
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
            this.help = "deletes an existing game and action rule";
            this.arguments = "<game>";
            this.guildOnly = false;
        }

        @Override
        protected final void execute(CommandEvent event) {
            String gameName = event.getArgs().replaceAll("\\s+", "_");

            try {
                bot.getGameAndActionSimulationManager().deleteRule(gameName);
                log.info("GAASimulation rule with game {} was removed by {}:[{}]", gameName, event.getAuthor().getName(), event.getAuthor().getId());
                event.replySuccess(String.format("Successfully deleted rule with game `%1$s`!", gameName));
            } catch (IOException ioe) {
                log.error("IO error during removal of GAASimulation rule", ioe);
                event.replyError(String.format("Unable to delete this rule, that has game: `[%1$s]`.", ioe.getLocalizedMessage()));
            }
        }
    }
}
