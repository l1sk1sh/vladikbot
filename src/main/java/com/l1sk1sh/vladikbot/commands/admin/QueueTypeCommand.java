package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.models.queue.QueueType;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author Wolfgang Schwendtbauer
 */
@Slf4j
@Service
public class QueueTypeCommand extends AdminCommand {

    private static final String TYPE_OPTION_KEY = "type";

    private final BotSettingsManager settings;

    @Autowired
    public QueueTypeCommand(BotSettingsManager settings) {
        this.settings = settings;
        this.name = "queuetype";
        this.help = "Changes the queue type";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, TYPE_OPTION_KEY, "Queue type").setRequired(true)
                .addChoice("Fair (duplicate-free queue)", "fair")
                .addChoice("Linear (one-by-one simple queue)", "linear")
        );
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        OptionMapping statusOption = event.getOption(TYPE_OPTION_KEY);
        String currentSetting = settings.get().getQueueType().getUserFriendlyName();
        if (statusOption == null) {
            event.replyFormat("Current queue type is `%1$s`", currentSetting).setEphemeral(true).queue();
            return;
        }

        String option = statusOption.getAsString();
        QueueType newSetting;
        try {
            newSetting = QueueType.valueOf(option.toUpperCase());
        } catch (IllegalArgumentException e) {
            event.replyFormat("%1$s Invalid queue type. Valid types are: [%2$s]",
                    event.getClient().getError(), String.join("|", QueueType.getNames())).setEphemeral(true).queue();
            return;
        }

        if (settings.get().getQueueType() == newSetting) {
            event.replyFormat("Current queue type is already `%1$s`", newSetting).setEphemeral(true).queue();
            return;
        }

        settings.get().setQueueType(newSetting);

        AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        if (handler != null) {
            handler.setQueueType(newSetting);
        }

        event.replyFormat("%1$s Queue type was set to `%2$s`",
                event.getClient().getSuccess(), newSetting.getUserFriendlyName()).setEphemeral(true).queue();
    }
}