package com.l1sk1sh.vladikbot.commands.dj;

import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
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
public class RepeatCommand extends DJCommand {

    private static final String REPEAT_OPTION_KEY = "repeat";

    private final BotSettingsManager settings;

    @Autowired
    public RepeatCommand(BotSettingsManager settings, GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.settings = settings;
        this.name = "mrepeat";
        this.help = "Re-adds music to the queue when finished";
        this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, REPEAT_OPTION_KEY, "Should queue be repeated").setRequired(false));
        this.guildOnly = true;
    }

    /* Override MusicCommand's execute because we don't actually care where this is used */
    @Override
    protected final void execute(SlashCommandEvent event) {
        boolean currentSetting = settings.get().isRepeat();

        OptionMapping repeatOption = event.getOption(REPEAT_OPTION_KEY);
        if (repeatOption == null) {
            event.replyFormat("Repeat is `%1$s`", (currentSetting ? "ON" : "OFF")).setEphemeral(true).queue();

            return;
        }

        boolean newSetting = repeatOption.getAsBoolean();

        if (currentSetting == newSetting) {
            event.replyFormat("Repeat is `%1$s`", (currentSetting ? "ON" : "OFF")).setEphemeral(true).queue();

            return;
        }

        settings.get().setRepeat(newSetting);

        event.replyFormat("Repeat is `%1$s`", (newSetting ? "ON" : "OFF")).queue();
    }

    @Override
    public final void doCommand(SlashCommandEvent event) { /* Intentionally empty */ }
}
