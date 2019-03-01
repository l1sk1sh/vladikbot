package com.multiheaded.disbot.command;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.List;

public abstract class AbstractCommand extends ListenerAdapter {
    public abstract void onCommand(MessageReceivedEvent e, String[] args);

    public abstract List<String> getAliases();

    public abstract String getDescription();

    public abstract String getName();

    public abstract List<String> getUsageInstructions();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() && !respondToBots()) {
            return;
        }
        if (containsCommand(event.getMessage())) {
            onCommand(event, commandArgs(event.getMessage()));
        }
    }

    protected Message sendMessage(MessageReceivedEvent e, Message message) {
        if (e.isFromType(ChannelType.PRIVATE)) {
            return e.getPrivateChannel().sendMessage(message).complete();
        } else {
            return e.getTextChannel().sendMessage(message).complete();
        }
    }

    protected Message sendMessage(MessageReceivedEvent e, String message) {
        return sendMessage(e, new MessageBuilder().append(message).build());
    }

    protected boolean containsCommand(Message message) {
        return getAliases().contains(commandArgs(message)[0]);
    }

    protected String[] commandArgs(Message message) {
        return commandArgs(message.getContentDisplay());
    }

    protected String[] commandArgs(String string) {
        return string.split(" ");
    }

    protected boolean respondToBots() {
        return false;
    }
}