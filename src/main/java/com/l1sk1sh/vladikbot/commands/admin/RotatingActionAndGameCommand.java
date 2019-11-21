package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.settings.Constants;
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
    protected void execute(CommandEvent event) {
        StringBuilder builder = new StringBuilder(event.getClient().getWarning()
                + " Action & Game Rotation Management Commands:\r\n");
        for (Command cmd : this.children) {
            builder.append("\r\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments()
                    == null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
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
        protected void execute(CommandEvent event) {
            try {
                if (event.getArgs().isEmpty()) {
                    event.replyError("Please include an **action** word!");
                    return;
                }

                String action;

                if (event.getArgs().toLowerCase().startsWith(Constants.ACTION_PLAYING)) {
                    action = Constants.ACTION_PLAYING;
                } else if (event.getArgs().toLowerCase().startsWith(Constants.ACTION_LISTENING)) {
                    action = Constants.ACTION_LISTENING;
                } else if (event.getArgs().toLowerCase().startsWith(Constants.ACTION_WATCHING)) {
                    action = Constants.ACTION_WATCHING;
                } else {
                    event.replyWarning(String.format("Action word must be one of `[%1$s, %2$s, %3$s]`!",
                            Constants.ACTION_PLAYING, Constants.ACTION_LISTENING, Constants.ACTION_WATCHING));
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
        protected void execute(CommandEvent event) {
            try {
                Map<String, String> pairs = bot.getActionAndGameRotationManager().getActionsAndGames();
                if (pairs == null) {
                    event.replyError("Failed to load available *action:game* pairs!");
                } else if (pairs.isEmpty()) {
                    event.replyWarning("There are no *action:game* records available!");
                } else {
                    StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Acting pairs:\r\n");

                    builder.append("`Action`");
                    builder.append("  ");
                    builder.append("`Game`");

                    for (String key : pairs.keySet()) {
                        if (builder.length() > 0) {
                            builder.append("\r\n");
                        }
                        String value = pairs.get(key);
                        try {
                            builder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
                            builder.append(" = ");
                            builder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException("Action requires UTF-8 encoding support!", e);
                        }
                    }
                    event.reply(builder.toString());
                }
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
        protected void execute(CommandEvent event) {
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
        protected void execute(CommandEvent event) {
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
