package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.services.processes.BackupDockerProcess;
import com.l1sk1sh.vladikbot.services.processes.CleanDockerContainerProcess;
import com.l1sk1sh.vladikbot.services.processes.CopyDockerFileProcess;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Oliver Johnson
 */
public class BackupTextChannelService implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(BackupTextChannelService.class);

    private final Bot bot;
    private File backupFile;
    private String beforeDate;
    private String afterDate;
    private String failMessage = "Failed due to unknown reason";
    private final String channelId;
    private final Const.BackupFileType format;
    private final String localPathToExport;
    private final String dockerPathToExport;
    private final String dockerContainerName;
    private final String token;
    private final Const.FileType extension;
    private boolean ignoreExistingBackup;
    private boolean hasFailed = true;

    public BackupTextChannelService(Bot bot, String channelId, Const.BackupFileType format, String localPathToExport,
                                    String beforeDate, String afterDate, boolean ignoreExistingBackup) {
        this.bot = bot;
        this.channelId = channelId;
        this.token = bot.getBotSettings().getToken();
        this.format = format;
        this.extension = format.getFileType();
        this.localPathToExport = localPathToExport + "text/"; /* Always moving text backups to separate folder */
        this.dockerPathToExport = bot.getBotSettings().getDockerPathToExport();
        this.dockerContainerName = bot.getBotSettings().getDockerContainerName();
        this.beforeDate = beforeDate;
        this.afterDate = afterDate;
        this.ignoreExistingBackup = ignoreExistingBackup;
    }

    @Override
    public void run() {
        try {
            FileUtils.createFolderIfAbsent(localPathToExport);

            bot.setLockedBackup(true);

            backupFile = FileUtils.getFileByChannelIdAndExtension(localPathToExport, channelId, extension);

            /* If file is present or was made less than 24 hours ago - exit */
            if ((backupFile != null && ((System.currentTimeMillis() - backupFile.lastModified()) < Const.DAY_IN_MILLISECONDS))
                    && ignoreExistingBackup) {
                log.info("Text backup has already been made [{}].", backupFile.getAbsolutePath());
                hasFailed = false;

                return;
            }

            log.info("Creating new backup for channel with ID '{}'.", channelId);

            log.info("Clearing docker container before execution...");
            runCleanProcess();

            log.info("Waiting for backup to finish...");
            runBackupProcess();

            log.info("Copying received backup file...");
            runCopyProcess();

            backupFile = FileUtils.getFileByChannelIdAndExtension(localPathToExport, channelId, extension);
            if (backupFile == null) {
                throw new FileNotFoundException("Failed to find or create backup of a channel");
            }

            log.debug("Text Channel Backup Service has finished its execution.");
            hasFailed = false;

        } catch (IOException ioe) {
            failMessage = String.format("Failed to find exported file [%1$s].", ioe.getLocalizedMessage());
            log.error(failMessage);
        } catch (InterruptedException ie) {
            failMessage = String.format("Backup thread interrupted on services level [%1$s].", ie.getLocalizedMessage());
            log.error(failMessage);
        } finally {
            try {
                log.info("Cleaning docker container after execution...");
                runCleanProcess();
            } catch (InterruptedException ire) {
                log.error("Clean process thread was interrupted:", ire);
            } catch (IOException ioe) {
                log.error("Cleaning failed due to IO:", ioe);
            } finally {
                bot.setLockedBackup(false);
            }
        }
    }

    private void runCleanProcess() throws IOException, InterruptedException {
        CleanDockerContainerProcess cleanProcess = new CleanDockerContainerProcess(dockerContainerName);
        int exitCode = cleanProcess.call();
        switch (exitCode) {
            case 0:
                log.info("Container was running and it was cleared.");
                break;
            case 2:
                log.info("Container for clearing was not found.");
                break;
            default:
                log.warn("Unhandled exit code for clear command: {}.", exitCode);
                break;
        }
    }

    private void runBackupProcess() throws IOException, InterruptedException {
        BackupDockerProcess backupProcess = new BackupDockerProcess(dockerContainerName, format,
                beforeDate, afterDate, channelId, token);
        int exitCode = backupProcess.call();
        //noinspection SwitchStatementWithTooFewBranches
        switch (exitCode) {
            case 0:
                log.info("BackupProcess returned exit code successful.");
                break;
            default:
                log.warn("Unhandled exit code for backup command: {}.", exitCode);
                break;
        }
    }

    private void runCopyProcess() throws IOException, InterruptedException {
        CopyDockerFileProcess copyProcess = new CopyDockerFileProcess(dockerContainerName, dockerPathToExport, localPathToExport);
        int exitCode = copyProcess.call();
        //noinspection SwitchStatementWithTooFewBranches
        switch (exitCode) {
            case 0:
                log.info("CopyProcess returned exit code successful.");
                break;
            default:
                log.warn("Unhandled exit code for copy command: {}.", exitCode);
                break;
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
