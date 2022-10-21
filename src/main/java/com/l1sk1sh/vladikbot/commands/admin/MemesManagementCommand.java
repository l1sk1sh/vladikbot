package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.meme.MemeService;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
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
public class MemesManagementCommand extends AdminCommand {

    private final MemeService memeService;
    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public MemesManagementCommand(MemeService memeService, GuildSettingsRepository guildSettingsRepository) {
        this.memeService = memeService;
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "memes";
        this.help = "Manage memes for this guild";
        this.children = new AdminCommand[]{
                new SwitchCommand(),
                new SetChannelCommand()
        };
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString()).setEphemeral(true).queue();
    }

    private final class SwitchCommand extends AdminCommand {

        private static final String SWITCH_OPTION_KEY = "switch";

        private SwitchCommand() {
            this.name = "switch";
            this.help = "Enables or disables memes update";
            this.guildOnly = false;
            this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, SWITCH_OPTION_KEY, "State of memes").setRequired(false));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());
            boolean currentSetting = settings.map(GuildSettings::isSendMemes).orElse(false);
            TextChannel memesChannel = settings.map(guildSettings -> guildSettings.getMemesChannel(event.getGuild())).orElse(null);

            OptionMapping switchOption = event.getOption(SWITCH_OPTION_KEY);
            if (switchOption == null) {
                event.replyFormat("Memes sending is `%1$s`", (currentSetting) ? "ON" : "OFF").setEphemeral(true).queue();

                return;
            }

            boolean newSetting = switchOption.getAsBoolean();

            if (memesChannel == null && newSetting) {
                event.replyFormat("%1$s Set memes channel first.", event.getClient().getWarning()).setEphemeral(true).queue();

                return;
            }

            if (currentSetting == newSetting) {
                event.replyFormat("Memes sending is `%1$s`", (currentSetting) ? "ON" : "OFF").setEphemeral(true).queue();

                return;
            }

            settings.ifPresent((guildSettings -> {
                guildSettings.setSendMemes(newSetting);
                guildSettingsRepository.save(settings.get());
            }));
            if (newSetting) {
                memeService.start();
            } else {
                memeService.stop();
            }

            log.info("Memes changed to {} by {}", newSetting, FormatUtils.formatAuthor(event));
            event.replyFormat("Memes are `%1$s`", (newSetting) ? "ON" : "OFF").setEphemeral(true).queue();
        }
    }

    private final class SetChannelCommand extends AdminCommand {

        private static final String CHANNEL_OPTION_KEY = "channel";

        private SetChannelCommand() {
            this.name = "channel";
            this.help = "Sets channel for memes submission";
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, CHANNEL_OPTION_KEY, "Memes channel").setRequired(false));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());
            TextChannel memesChannel = settings.map(guildSettings -> guildSettings.getMemesChannel(event.getGuild())).orElse(null);

            OptionMapping channelOption = event.getOption(CHANNEL_OPTION_KEY);
            if (channelOption == null) {
                event.replyFormat("Memes are being sent to `%1$s`", (memesChannel != null) ? memesChannel.getAsMention() : "nowhere").setEphemeral(true).queue();

                return;
            }

            String newChannelId = channelOption.getAsString();

            List<TextChannel> list = FinderUtil.findTextChannels(newChannelId, event.getGuild());
            if (list.isEmpty()) {
                event.replyFormat("%1$s No Text Channels found matching \"%2$s\".", event.getClient().getWarning(), newChannelId).setEphemeral(true).queue();
            } else {
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                    setting.setMemesChannelId(list.get(0).getIdLong());
                    guildSettingsRepository.save(setting);
                    log.info("Memes channel was set to {}. Set by {}.", list.get(0).getId(), FormatUtils.formatAuthor(event));
                    event.replyFormat("%1$s Memes are being displayed in <#%2$s>.", event.getClient().getSuccess(), list.get(0).getId()).setEphemeral(true).queue();
                });
            }
        }
    }
}
