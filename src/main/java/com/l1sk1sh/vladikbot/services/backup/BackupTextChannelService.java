package com.l1sk1sh.vladikbot.services.backup;

import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author l1sk1sh
 */
public class BackupTextChannelService implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(BackupTextChannelService.class);

    private final BotSettingsManager settings;
    private final DockerService dockerService;
    private File backupFile;
    private final String beforeDate;
    private final String afterDate;
    private String failMessage = "Failed due to unexpected error";
    private final String channelId;
    private final Const.BackupFileType format;
    private final String localPathToExport;
    private final String token;
    private final Const.FileType extension;
    private final boolean useExistingBackup;
    private boolean hasFailed = true;

    public BackupTextChannelService(BotSettingsManager settings, DockerService dockerService, String channelId, Const.BackupFileType format, String localPathToExport,
                                    String beforeDate, String afterDate, boolean useExistingBackup) {
        this.settings = settings;
        this.dockerService = dockerService;
        this.channelId = channelId;
        this.token = settings.get().getToken();
        this.format = format;
        this.extension = format.getFileType();
        this.localPathToExport = localPathToExport + channelId + "/" + FormatUtils.getNormalizedCurrentDate() + "/";
        this.beforeDate = beforeDate;
        this.afterDate = afterDate;
        this.useExistingBackup = useExistingBackup;
    }

    @Override
    public void run() {
        try {
            FileUtils.createFolderIfAbsent(localPathToExport);

            settings.get().setLockedBackup(true);

            backupFile = FileUtils.getFileByChannelIdAndExtension(localPathToExport, channelId, extension);

            /* If file is present or was made less than 24 hours ago - exit */
            if ((backupFile != null && ((System.currentTimeMillis() - backupFile.lastModified()) < Const.DAY_IN_MILLISECONDS))
                    && useExistingBackup) {
                log.info("Text backup has already been made [{}].", backupFile.getAbsolutePath());
                hasFailed = false;

                return;
            }

            log.info("Creating new backup for channel with ID '{}'.", channelId);

            log.info("Waiting for backup to finish...");
            if (!dockerService.runBackup(format, beforeDate, afterDate, channelId, token)) {
                failMessage = "Backup did not finish";

                log.debug("Docker logs output:\r\n{}", dockerService.getContainerLogs());

                return;
            }

            log.info("Copying received backup file...");
            backupFile = dockerService.copyBackupFile(localPathToExport);
            if (backupFile == null) {
                failMessage = "Failed to find or create backup of a channel";

                return;
            }

            log.debug("Text Channel Backup Service has finished its execution.");
            hasFailed = false;

        } catch (IOException ioe) {
            failMessage = String.format("Failed to find exported file [%1$s].", ioe.getLocalizedMessage());
            log.error(failMessage);
        } finally {
            settings.get().setLockedBackup(false);
        }
    }

    public final File getBackupFile() {
        return backupFile;
    }

    public final String getFailMessage() {
        return failMessage;
    }

    public final boolean hasFailed() {
        return hasFailed;
    }
}
