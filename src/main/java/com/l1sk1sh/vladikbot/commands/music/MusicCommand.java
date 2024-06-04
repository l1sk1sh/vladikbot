package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
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
        this.category = new Category("Music");
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Guild currentGuild = event.getGuild();
        if (currentGuild == null) {
            event.replyFormat("%1$s This command should not be called in DMs!", event.getClient().getError()).queue();

            return;
        }

        Optional<GuildSettings> settings = guildSettingsRepository.findById(currentGuild.getIdLong());
        TextChannel textChannel = settings.map(guildSettings -> guildSettings.getTextChannel(currentGuild)).orElse(null);

        if (textChannel != null && !event.getTextChannel().equals(textChannel)) {
            event.replyFormat("%1$s You can only use that command in %2$s!", event.getClient().getWarning(), textChannel.getAsMention()).setEphemeral(true).queue();

            return;
        }

        playerManager.setUpHandler(event.getGuild()); /* No point in constantly checking for this later */
        if (bePlaying && !((AudioHandler) Objects.requireNonNull(event.getGuild().getAudioManager().getSendingHandler()))
                .isMusicPlaying(event.getJDA())) {
            event.replyFormat("%1$s There must be music playing to use that!", event.getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        if (beListening) {
            AudioChannelUnion possible =
                    Objects.requireNonNull(
                            event.getGuild().getSelfMember().getVoiceState()
                    ).getChannel();
            VoiceChannel current;
            if (possible == null) {
                current = settings.map(guildSettings -> guildSettings.getVoiceChannel(event.getGuild())).orElse(null);
            } else {
                current = possible.asVoiceChannel();
            }

            GuildVoiceState userState = Objects.requireNonNull(event.getMember()).getVoiceState();
            if (!Objects.requireNonNull(userState).inAudioChannel()
                    || userState.isDeafened()
                    || (current != null && !Objects.requireNonNull(userState.getChannel()).equals(current))) {
                event.replyFormat("%1$s You must be listening in %2$s to use that!",
                        event.getClient().getWarning(),
                        (current == null ? "a voice channel" : current.getAsMention())
                ).setEphemeral(true).queue();

                return;
            }

            VoiceChannel afkChannel = userState.getGuild().getAfkChannel();
            if (afkChannel != null && afkChannel.equals(userState.getChannel())) {
                event.replyFormat("%1$s You cannot use that command in an AFK channel!", event.getClient().getWarning()).setEphemeral(true).queue();

                return;
            }

            if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
                try {
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                    event.getGuild().getAudioManager().setSelfDeafened(true);
                } catch (PermissionException ex) {
                    event.replyFormat("%1$s Unable to connect to %2$s!",
                            event.getClient().getWarning(),
                            Objects.requireNonNull(userState.getChannel()).getAsMention()
                    ).setEphemeral(true).queue();

                    return;
                }
            }
        }

        doCommand(event);
    }

    protected abstract void doCommand(SlashCommandEvent event);
}
