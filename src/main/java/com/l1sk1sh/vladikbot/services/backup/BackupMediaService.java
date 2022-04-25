package com.l1sk1sh.vladikbot.services.backup;

import com.l1sk1sh.vladikbot.VladikBot;
import com.l1sk1sh.vladikbot.data.entity.DiscordAttachment;
import com.l1sk1sh.vladikbot.data.repository.DiscordAttachmentsRepository;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.DownloadUtils;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class BackupMediaService {

    private static final String BACKUP_MEDIA_DIR = "media";

    private final ScheduledExecutorService backupThreadPool;
    private final BotSettingsManager settings;
    private final DiscordAttachmentsRepository discordAttachmentsRepository;

    private String backupDirPath;

    @Autowired
    public BackupMediaService(@Qualifier("backupThreadPool") ScheduledExecutorService backupThreadPool,
                              BotSettingsManager settings, DiscordAttachmentsRepository discordAttachmentsRepository) {
        this.settings = settings;
        this.discordAttachmentsRepository = discordAttachmentsRepository;
        this.backupThreadPool = backupThreadPool;
    }

    public void init() {
        backupDirPath = settings.get().getWorkdir() + "/backup/" + BACKUP_MEDIA_DIR + "/";
        try {
            FileUtils.createFolderIfAbsent(backupDirPath);
        } catch (IOException e) {
            log.error("Failed to create initial media backup directory.", e);
        }
    }

    public void downloadAllAttachments(OnBackupCompletedListener listener) {
        backupThreadPool.execute(() -> {
            log.info("Downloading all attachments...");
            JDA jda = VladikBot.jda();

            try {
                for (Guild guild : jda.getGuilds()) {
                    log.debug("Reading message history for guild '{}'", guild.getName());

                    for (TextChannel channel : guild.getTextChannels()) {
                        String channelBackupDirPath;
                        try {
                            channelBackupDirPath = backupDirPath + channel.getIdLong() + "/";
                            FileUtils.createFolderIfAbsent(channelBackupDirPath);
                        } catch (IOException e) {
                            log.error("Failed to create media backup directory for channel '{}'", channel.getName(), e);
                            continue;
                        }

                        try {
                            List<DiscordAttachment> channelAttachments
                                    = discordAttachmentsRepository.getAllNotDownloadedByChannelId(channel.getIdLong());

                            if (channelAttachments == null || channelAttachments.isEmpty()) {
                                throw new EntityNotFoundException();
                            }

                            for (DiscordAttachment attachment : channelAttachments) {

                                if (StringUtils.stringContainsItemFromList(DownloadUtils.getFilenameFromUrl(attachment.getUrl()), Const.NAME_INVALID_CHARS)) {
                                    continue;
                                }

                                if (StringUtils.stringContainsItemFromList(attachment.getUrl(), Const.FileType.getRawSupportedMediaFormatsAsArray())) {
                                    downloadAttachment(attachment, channelBackupDirPath);
                                }
                            }

                        } catch (EntityNotFoundException e) {
                            log.warn("Failed to get attachments for channel '{}'.", channel.getName(), e);
                        }
                    }
                }

                listener.onBackupCompleted(true, "");
            } catch (RuntimeException e) {
                log.error("Failed to finish complete bot media backup.", e);
                listener.onBackupCompleted(false, e.getLocalizedMessage());
            }
        });
    }

    private void downloadAttachment(DiscordAttachment attachment, String channelBackupDirPath) {
        try {
            if (DownloadUtils.downloadAndSaveToFile(new URL(attachment.getUrl()), channelBackupDirPath + attachment.getFileName())) {
                attachment.setDownloaded(true);
                discordAttachmentsRepository.save(attachment);
            } else {
                log.warn("Failed to save attachment [{}].", attachment);
            }
        } catch (InvalidPathException | MalformedURLException e) {
            log.warn("Failed to save attachment [{}] due to invalid path or url.", attachment, e);
        } catch (IOException e) {
            log.warn("Failed to save attachment [{}].", attachment, e);
        }
    }

    public void exportMediaToHtmlFile(OnFileCreatedListener listener, long channelId) {
        backupThreadPool.execute(() -> {
            log.info("Exporting channel '{}' attachments to HTML file...", channelId);

            try {
                List<DiscordAttachment> channelAttachments
                        = discordAttachmentsRepository.getAllByChannelId(channelId);

                if (channelAttachments == null || channelAttachments.isEmpty()) {
                    throw new EntityNotFoundException();
                }

                File htmlFile = writeAttachmentsUrlsToHtmlFile(channelAttachments, channelId);
                listener.onFileCreated(true, "", htmlFile);
            } catch (EntityNotFoundException e) {
                log.warn("Failed to get downloaded attachments for channel '{}'.", channelId, e);
                listener.onFileCreated(false, "Database doesn't have records for required channel", null);
            } catch (IOException e) {
                log.warn("Failed to create html file for channel '{}'.", channelId, e);
                listener.onFileCreated(false, "Error during file creation", null);
            }
        });
    }

    private File writeAttachmentsUrlsToHtmlFile(List<DiscordAttachment> attachments, long channelId) throws IOException {
        String pathToHtmlFile = backupDirPath + channelId + "." + Const.FileType.html.name();

        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!doctype html><html lang=\"en\"><head>");
        htmlContent.append(String.format("<title>%1$s</title>", channelId));
        htmlContent.append("</head><style>img {border: 1px solid #ddd;border-radius: 4px;");
        htmlContent.append("padding: 5px;width: 150px;}img:hover {");
        htmlContent.append("box-shadow: 0 0 2px 1px rgba(0, 140, 186, 0.5);}</style><body>");
        for (String url : attachments.stream().map(DiscordAttachment::getUrl).collect(Collectors.toList())) {
            htmlContent.append(String.format("<a target=\"_blank\" href=\"%1$s\"><img src=\"%2$s\"></a>", url, url));
        }
        htmlContent.append("</body></html>");
        Files.write(Paths.get(pathToHtmlFile), htmlContent.toString().getBytes());
        return new File(pathToHtmlFile);
    }

    public void resetDownloadedAttachments() {
        backupThreadPool.execute(discordAttachmentsRepository::resetAllDownloadedAttachments);
    }
}
