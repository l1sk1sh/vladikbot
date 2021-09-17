package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * - Moving to JDA-Chewtils
 * @author John Grosh
 */
@Service
public abstract class MusicCommand extends SlashCommand {

    protected final GuildSettingsRepository guildSettingsRepository;
    protected final PlayerManager playerManager;

    protected boolean bePlaying;
    protected boolean beListening;

    @Autowired
    protected MusicCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        this.guildSettingsRepository = guildSettingsRepository;
        this.playerManager = playerManager;
        this.guildOnly = true;
        this.category = new Category("Music");
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Guild currentGuild = event.getGuild();
        if (currentGuild == null) {
            event.replyFormat("%1$s This command should not be called in DMs!", getClient().getError()).queue();

            return;
        }

        Optional<GuildSettings> settings = guildSettingsRepository.findById(currentGuild.getIdLong());
        TextChannel textChannel = settings.map(guildSettings -> guildSettings.getTextChannel(currentGuild)).orElse(null);

        if (textChannel != null && !event.getTextChannel().equals(textChannel)) {
            event.replyFormat("%1$s You can only use that command in %2$s!", getClient().getWarning(), textChannel.getAsMention()).setEphemeral(true).queue();

            return;
        }

        playerManager.setUpHandler(event.getGuild()); /* No point in constantly checking for this later */
        if (bePlaying && !((AudioHandler) Objects.requireNonNull(event.getGuild().getAudioManager().getSendingHandler()))
                .isMusicPlaying(event.getJDA())) {
            event.replyFormat("%1$s There must be music playing to use that!", getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        if (beListening) {
            VoiceChannel current = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel();
            if (current == null) {
                current = settings.map(guildSettings -> guildSettings.getVoiceChannel(event.getGuild())).orElse(null);
            }

            GuildVoiceState userState = Objects.requireNonNull(event.getMember()).getVoiceState();
            if (!Objects.requireNonNull(userState).inVoiceChannel()
                    || userState.isDeafened()
                    || (current != null && !Objects.requireNonNull(userState.getChannel()).equals(current))) {
                event.replyFormat("%1$s You must be listening in *%2$s* to use that!",
                        getClient().getWarning(),
                        (current == null ? "a voice channel" : current.getName())
                ).setEphemeral(true).queue();

                return;
            }

            VoiceChannel afkChannel = userState.getGuild().getAfkChannel();
            if (afkChannel != null && afkChannel.equals(userState.getChannel())) {
                event.replyFormat("%1$s You cannot use that command in an AFK channel!", getClient().getWarning()).setEphemeral(true).queue();

                return;
            }

            if (!event.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
                try {
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                } catch (PermissionException ex) {
                    event.replyFormat("%1$s Unable to connect to **%2$s**!",
                            getClient().getWarning(),
                            Objects.requireNonNull(userState.getChannel()).getName()
                    ).setEphemeral(true).queue();

                    return;
                }
            }
        }

        doCommand(event);
    }

    protected abstract void doCommand(SlashCommandEvent event);
}
