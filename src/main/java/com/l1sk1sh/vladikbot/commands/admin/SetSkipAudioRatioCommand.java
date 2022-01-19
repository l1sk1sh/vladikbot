package com.l1sk1sh.vladikbot.commands.admin;

import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class SetSkipAudioRatioCommand extends AdminV2Command {
    private static final Logger log = LoggerFactory.getLogger(SetSkipAudioRatioCommand.class);

    private static final String SKIP_RATIO_OPTION_KEY = "ratio";

    private final BotSettingsManager settings;

    @Autowired
    public SetSkipAudioRatioCommand(BotSettingsManager settings) {
        this.settings = settings;
        this.name = "setsr";
        this.help = "Sets a server-specific skip percentage";
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, SKIP_RATIO_OPTION_KEY, "New volume for player between [0 - 100]").setRequired(false));
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        double currentSetting = settings.get().getAudioSkipRatio();

        OptionMapping volumeOption = event.getOption(SKIP_RATIO_OPTION_KEY);
        if (volumeOption == null) {
            event.replyFormat("Current skip ratio is `%1$s`.", currentSetting).setEphemeral(true).queue();

            return;
        }

        try {
            long skipRatio = volumeOption.getAsLong();
            if (skipRatio < 0 || skipRatio > 100) {
                event.replyFormat("%1$s Provided value must be between 0 and 100!", getClient().getWarning()).setEphemeral(true).queue();

                return;
            }

            settings.get().setAudioSkipRatio(skipRatio / 100.0);
            log.info("Audio skip ratio has been set to {}. Set by {}.", skipRatio / 100.0, FormatUtils.formatAuthor(event));
            event.replyFormat("Skip percentage has been set to `%1$s%` of listeners").queue();
        } catch (NumberFormatException ex) {
            event.replyFormat("%1$s Please include an integer between 0 and 100 (default is 55). This number is the percentage of listening users that must vote to skip a song.", getClient().getError()).setEphemeral(true).queue();
        }
    }
}
