package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.notification.NewsNotificationService;
import com.l1sk1sh.vladikbot.services.rss.RssService;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
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
public class NewsManagementCommand extends AdminCommand {

    private final RssService rssService;
    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public NewsManagementCommand(RssService rssService, GuildSettingsRepository guildSettingsRepository) {
        this.rssService = rssService;
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "news";
        this.help = "Manage news for this guild";
        this.children = new AdminCommand[]{
                new SwitchCommand(),
                new SetChannelCommand(),
                new SetStyleCommand()
        };
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(this, children, name).toString()).setEphemeral(true).queue();
    }

    private final class SwitchCommand extends AdminCommand {

        private static final String SWITCH_OPTION_KEY = "switch";

        private SwitchCommand() {
            this.name = "switch";
            this.help = "Enables or disables news update";
            this.guildOnly = false;
            this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, SWITCH_OPTION_KEY, "State of news").setRequired(false));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());
            boolean currentSetting = settings.map(GuildSettings::isSendNews).orElse(false);
            TextChannel newsChannel = settings.map(guildSettings -> guildSettings.getNewsChannel(event.getGuild())).orElse(null);

            OptionMapping switchOption = event.getOption(SWITCH_OPTION_KEY);
            if (switchOption == null) {
                event.replyFormat("News sending is `%1$s`", (currentSetting) ? "ON" : "OFF").setEphemeral(true).queue();

                return;
            }

            boolean newSetting = switchOption.getAsBoolean();

            if (newsChannel == null && newSetting) {
                event.replyFormat("%1$s Set news channel first.", getClient().getWarning()).setEphemeral(true).queue();

                return;
            }

            if (currentSetting == newSetting) {
                event.replyFormat("News sending is `%1$s`", (currentSetting) ? "ON" : "OFF").setEphemeral(true).queue();

                return;
            }

            settings.ifPresent((guildSettings -> {
                guildSettings.setSendNews(newSetting);
                guildSettingsRepository.save(settings.get());
            }));
            if (newSetting) {
                rssService.start();
            } else {
                rssService.stop();
            }

            log.info("News changed to {} by {}", newSetting, FormatUtils.formatAuthor(event));
            event.replyFormat("News sending is `%1$s`", (newSetting) ? "ON" : "OFF").setEphemeral(true).queue();
        }
    }

    private final class SetChannelCommand extends AdminCommand {

        private static final String CHANNEL_OPTION_KEY = "channel";

        private SetChannelCommand() {
            this.name = "channel";
            this.help = "Sets channel for news submission";
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, CHANNEL_OPTION_KEY, "News channel. ").setRequired(false));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());
            TextChannel newsChannel = settings.map(guildSettings -> guildSettings.getNewsChannel(event.getGuild())).orElse(null);

            OptionMapping channelOption = event.getOption(CHANNEL_OPTION_KEY);
            if (channelOption == null) {
                event.replyFormat("News are being sent to `%1$s`", (newsChannel != null) ? newsChannel.getAsMention() : "nowhere").setEphemeral(true).queue();

                return;
            }

            String newChannelId = channelOption.getAsString();

            List<TextChannel> list = FinderUtil.findTextChannels(newChannelId, event.getGuild());
            if (list.isEmpty()) {
                event.replyFormat("%1$s No Text Channels found matching \"%2$s\".", getClient().getWarning(), newChannelId).setEphemeral(true).queue();
            } else {
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                    setting.setNewsChannelId(list.get(0).getIdLong());
                    guildSettingsRepository.save(setting);
                    log.info("News channel was set to {}. Set by {}.", list.get(0).getId(), FormatUtils.formatAuthor(event));
                    event.replyFormat("%1$s News are being displayed in <#%2$s>.", getClient().getSuccess(), list.get(0).getId()).setEphemeral(true).queue();
                });
            }
        }
    }

    private final class SetStyleCommand extends AdminCommand {

        private static final String STYLE_OPTION_KEY = "style";

        private SetStyleCommand() {
            this.name = "style";
            this.help = "Sets style for news submission";
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, STYLE_OPTION_KEY, "News style.").setRequired(false)
                    .addChoice("Style with description", "full")
                    .addChoice("Style with single title", "short")
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());
            NewsNotificationService.NewsStyle currentNewsStyle = settings.map(GuildSettings::getNewsStyle).orElse(GuildSettings.DEFAULT_NEWS_STYLE);

            OptionMapping styleOption = event.getOption(STYLE_OPTION_KEY);
            if (styleOption == null) {
                event.replyFormat("Current news style is `%1$s`", currentNewsStyle).setEphemeral(true).queue();

                return;
            }

            String newsStyle = styleOption.getAsString();
            NewsNotificationService.NewsStyle style;
            try {
                style = NewsNotificationService.NewsStyle.valueOf(newsStyle.toUpperCase());
            } catch (IllegalArgumentException e) {
                event.replyFormat("%1$s Specify either `full` or `short` style.", getClient().getWarning()).setEphemeral(true).queue();

                return;
            }

            settings.ifPresent((guildSettings -> {
                guildSettings.setNewsStyle(style);
                guildSettingsRepository.save(guildSettings);
            }));

            log.info("News style is set to '{}' by '{}'.", style, FormatUtils.formatAuthor(event));
            event.replyFormat("%1$s Changed current news style to `%2$s`.", getClient().getSuccess(), style.name().toLowerCase()).queue();
        }
    }
}
