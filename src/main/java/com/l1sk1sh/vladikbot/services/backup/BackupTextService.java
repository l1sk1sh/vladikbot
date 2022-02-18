package com.l1sk1sh.vladikbot.services.backup;

import com.l1sk1sh.vladikbot.VladikBot;
import com.l1sk1sh.vladikbot.data.entity.DiscordMessage;
import com.l1sk1sh.vladikbot.data.repository.DiscordMessagesRepository;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.MapperUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * Current service is responsible for checking already backed up messages,
 * listing chat and making it's backup
 *
 * @author l1sk1sh
 */
@Slf4j
@Service
public class BackupTextService {

    private final ScheduledExecutorService backupThreadPool;
    private final BotSettingsManager settings;
    private final DiscordMessagesRepository discordMessagesRepository;

    @Autowired
    public BackupTextService(@Qualifier("backupThreadPool") ScheduledExecutorService backupThreadPool,
                             BotSettingsManager settings, DiscordMessagesRepository discordMessagesRepository) {
        this.settings = settings;
        this.discordMessagesRepository = discordMessagesRepository;
        this.backupThreadPool = backupThreadPool;
    }

    public void backupNewMessage(Message newMessage) {
        if (newMessage.getType() != MessageType.DEFAULT) {
            return;
        }
        backupThreadPool.execute(() -> writeMessage(newMessage));
    }

    public void addReaction(MessageReaction reaction) {
        backupThreadPool.execute(() -> {
            Optional<DiscordMessage> existingMessage = discordMessagesRepository.findById(reaction.getMessageIdLong());
            existingMessage.ifPresentOrElse(
                    message -> {
                        message.getReactions().add(MapperUtils.mapMessageReactionToDiscordReaction(reaction, reaction.getMessageIdLong()));
                        discordMessagesRepository.save(message);
                    },
                    () -> log.warn("Failed to add reaction '{}' due to missing in internal DB message with ID '{}'.",
                            reaction, reaction.getMessageIdLong())
            );
        });
    }

    public void readAllChannelsHistories(OnBackupCompletedListener listener) {
        backupThreadPool.execute(() -> {
            log.info("Reading channels histories...");
            JDA jda = VladikBot.jda();

            try {
                for (Guild guild : jda.getGuilds()) {
                    log.debug("Reading message history for guild '{}'", guild.getName());

                    for (TextChannel channel : guild.getTextChannels()) {
                        try {
                            MessageHistory messageHistory = channel.getHistoryFromBeginning(100).complete();
                            readMessageHistory(messageHistory, messageHistory.size());
                            writeMessageHistory(messageHistory);
                        } catch (MissingAccessException e) {
                            log.warn("Channel '{}' cannot be read due to missing permission.", channel.getName(), e);
                        }
                    }
                }

                listener.onBackupCompleted(true, "");
            } catch (RuntimeException e) {
                log.error("Failed to finish complete bot backup.", e);
                listener.onBackupCompleted(false, e.getLocalizedMessage());
            }
        });
    }

    private void readMessageHistory(MessageHistory messageHistory, int previousSize) {
        messageHistory.retrieveFuture(100).complete();
        int currentSize = messageHistory.getRetrievedHistory().size();
        if (currentSize == previousSize) {
            return;
        }

        readMessageHistory(messageHistory, currentSize);
    }

    private void writeMessage(Message message) {
        discordMessagesRepository.save(MapperUtils.mapMessageToDiscordMessage(message));
    }

    private void writeMessageHistory(MessageHistory messageHistory) {
        List<DiscordMessage> messages = messageHistory.getRetrievedHistory().stream()
                .filter(message -> message.getType() == MessageType.DEFAULT)
                .map(MapperUtils::mapMessageToDiscordMessage).collect(Collectors.toList());

        discordMessagesRepository.saveAll(messages);
    }
}
