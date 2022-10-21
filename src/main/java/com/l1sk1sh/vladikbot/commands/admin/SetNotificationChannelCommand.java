package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class SetNotificationChannelCommand extends AdminCommand {

    private static final String CHANNEL_OPTION_KEY = "channel";

    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public SetNotificationChannelCommand(GuildSettingsRepository guildSettingsRepository) {
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "setnc";
        this.help = "Sets the text channel for notifications from bot";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, CHANNEL_OPTION_KEY, "Notification channel. Set 'none' to remove one").setRequired(false));
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());
        TextChannel notificationChannel = settings.map(guildSettings -> guildSettings.getNotificationChannel(event.getGuild())).orElse(null);

        OptionMapping channelOption = event.getOption(CHANNEL_OPTION_KEY);
        if (channelOption == null) {
            event.replyFormat("Notifications are being sent to `%1$s`", (notificationChannel != null) ? notificationChannel.getAsMention() : "nowhere").setEphemeral(true).queue();

            return;
        }

        String notificationChannelId = channelOption.getAsString();
        if (notificationChannelId.equals("0") || notificationChannelId.equalsIgnoreCase("none")) {
            guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                setting.setNotificationChannelId(0L);
                guildSettingsRepository.save(setting);
                log.info("Notification channel was removed. Set by {}.", FormatUtils.formatAuthor(event));
                event.replyFormat("%1$s Bot-specific and technical notifications are disabled.", event.getClient().getSuccess()).setEphemeral(true).queue();
            });
        } else {
            List<TextChannel> list = FinderUtil.findTextChannels(notificationChannelId, event.getGuild());
            if (list.isEmpty()) {
                event.replyFormat("%1$s No Text Channels found matching \"%2$s\".", event.getClient().getWarning(), notificationChannelId).setEphemeral(true).queue();
            } else {
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                    setting.setNotificationChannelId(list.get(0).getIdLong());
                    guildSettingsRepository.save(setting);
                    log.info("Notification channel was set to {}. Set by {}.", list.get(0).getId(), FormatUtils.formatAuthor(event));
                    event.replyFormat("%1$s Notifications are being displayed in <#%2$s>.", event.getClient().getSuccess(), list.get(0).getId()).setEphemeral(true).queue();
                });
            }
        }
    }
}
