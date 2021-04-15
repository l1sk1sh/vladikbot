package com.l1sk1sh.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class VolumeCommand extends DJCommand {
    private static final int MAX_VOLUME = 150;

    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public VolumeCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "volume";
        this.aliases = new String[]{"vol"};
        this.help = "sets or shows volume";
        this.arguments = "[0-" + MAX_VOLUME + "]";
    }

    @Override
    public final void doCommand(CommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        int volume = Objects.requireNonNull(audioHandler).getPlayer().getVolume();

        if (event.getArgs().isEmpty()) {
            event.reply(String.format("%1$s Current volume is `%2$s`.", FormatUtils.volumeIcon(volume), volume));
        } else {
            int nVolume;
            try {
                nVolume = Integer.parseInt(event.getArgs());
            } catch (NumberFormatException e) {
                nVolume = -1;
            }

            if (nVolume < 0 || nVolume > MAX_VOLUME) {
                event.replyError(String.format("Volume must be a valid integer between 0 and `%1$d`!", MAX_VOLUME));
            } else {
                audioHandler.getPlayer().setVolume(nVolume);
                int finalNVolume = nVolume;
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(settings -> {
                    settings.setVolume(finalNVolume);
                    guildSettingsRepository.save(settings);
                    event.reply(String.format("%1$s Volume changed from `%2$s` to `%3$s`.",
                            FormatUtils.volumeIcon(finalNVolume), volume, finalNVolume));
                });
            }
        }
    }

}
