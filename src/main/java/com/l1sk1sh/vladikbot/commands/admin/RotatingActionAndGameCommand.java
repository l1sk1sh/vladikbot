package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.Bot;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author Oliver Johnson
 */
public class RotatingActionAndGameCommand extends AdminCommand {
    private final Bot bot;

    public RotatingActionAndGameCommand(Bot bot) {
        this.bot = bot;
        this.name = "actions";
        this.aliases = new String[]{"games", "rotate", "rotation"};
        this.help = "action:game rotation management";
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
        String message = event.getClient().getWarning() + " Action & Game Rotation Management Commands:\r\n";
        StringBuilder builder = new StringBuilder(message);
        for (Command cmd : this.children) {
            builder.append("\r\n`")
                    .append(event.getClient().getPrefix())
                    .append(name)
                    .append(" ")
                    .append(cmd.getName())
                    .append(" ")
                    .append(cmd.getArguments() == null
                            ? ""
                            : cmd.getArguments())
                    .append("` - ")
                    .append(cmd.getHelp());
        }
        event.reply(builder.toString());
    }

    class CreateCommand extends AdminCommand {
        CreateCommand() {
            this.name = "create";
            this.aliases = new String[]{"make", "add"};
            this.help = "creates new *action:game* message pair";
            this.arguments = "<action> <game>";
            this.guildOnly = false;
        }

        @Override
        protected final void execute(CommandEvent event) {
            try {
                if (event.getArgs().isEmpty()) {
                    event.replyError("Please include an **action** word!");
                    return;
                }

                String action;

                if (event.getArgs().toLowerCase().startsWith(Const.ACTION_PLAYING)) {
                    action = Const.ACTION_PLAYING;
                } else if (event.getArgs().toLowerCase().startsWith(Const.ACTION_LISTENING)) {
                    action = Const.ACTION_LISTENING;
                } else if (event.getArgs().toLowerCase().startsWith(Const.ACTION_WATCHING)) {
                    action = Const.ACTION_WATCHING;
                } else {
                    event.replyWarning(String.format("Action word must be one of `[%1$s, %2$s, %3$s]`!",
                            Const.ACTION_PLAYING, Const.ACTION_LISTENING, Const.ACTION_WATCHING));
                    return;
                }

                bot.getActionAndGameRotationManager()
                        .writeActionAndGame(action, event.getArgs().substring(action.length()).trim());
                event.replySuccess("New *action:game* pair was added.");
            } catch (IOException ioe) {
                event.replyError(String.format("Failed to write new *action:game*! `[%1$s]`", ioe.getLocalizedMessage()));
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
                Map<String, String> pairs = bot.getActionAndGameRotationManager().getActionsAndGames();
                if (pairs == null) {
                    event.replyError("Failed to load available *action:game* pairs!");
                } else if (pairs.isEmpty()) {
                    event.replyWarning("There are no *action:game* records available!");
                } else {
                    String message = event.getClient().getSuccess() + " Acting pairs:\r\n";
                    StringBuilder builder = new StringBuilder(message);

                    builder.append("`Action`");
                    builder.append("  ");
                    builder.append("`Game`");

                    for (Map.Entry<String, String> entry : pairs.entrySet()) {
                        if (builder.length() > 0) {
                            builder.append("\r\n");
                        }

                        builder.append(entry.getValue() != null ? URLEncoder.encode(entry.getValue(), "UTF-8") : "");
                        builder.append(" = ");
                        builder.append((entry.getKey() != null ? URLEncoder.encode(entry.getKey(), "UTF-8") : ""));
                    }

                    event.reply(builder.toString());
                }
            } catch (UnsupportedEncodingException e) {
                event.replyError("Action requires UTF-8 encoding support!");
            } catch (IOException ioe) {
                event.replyError(String.format("Local folder couldn't be processed! `[%1$s]`", ioe.getLocalizedMessage()));
            }
        }
    }

    class SwitchCommand extends AdminCommand {
        SwitchCommand() {
            this.name = "switch";
            this.aliases = new String[]{"change"};
            this.help = "enables or disables *action:game* rotation";
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
                            bot.getBotSettings().setRotateActionsAndGames(true);
                            event.replySuccess("Rotation is now enabled!");
                            bot.getActionAndGameRotationManager().activateRotation();
                            break;
                        case "off":
                        case "disable":
                            bot.getBotSettings().setRotateActionsAndGames(false);
                            event.replySuccess("Rotation is now disabled!");
                            bot.getActionAndGameRotationManager().stopRotation();
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
            this.help = "deletes an existing *action:game* pair";
            this.arguments = "<game>";
            this.guildOnly = false;
        }

        @Override
        protected final void execute(CommandEvent event) {
            String gameName = event.getArgs().replaceAll("\\s+", "_");

            if (bot.getActionAndGameRotationManager().getActionByGameTitle(gameName) == null) {
                event.replyError(String.format("Game `%1$s` doesn't exist!", gameName));
            } else {
                try {
                    bot.getActionAndGameRotationManager()
                            .deleteActionAndGame(bot.getActionAndGameRotationManager().getActionByGameTitle(gameName), gameName);
                    event.replySuccess(String.format("Successfully deleted *action:game* `%1$s`!", gameName));
                } catch (IOException e) {
                    event.replyError(String.format("Unable to delete this *action:game*: `[%1$s]`.", e.getLocalizedMessage()));
                }
            }
        }
    }
}
