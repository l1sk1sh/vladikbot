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
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class BackupTextChannelService implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(BackupTextChannelService.class);

    private final Bot bot;
    private File backupFile;
    private String beforeDate;
    private String afterDate;
    private String failMessage;
    private final String[] args;
    private final String channelId;
    private final Const.BackupFileType format;
    private final String localPathToExport;
    private final String dockerPathToExport;
    private final String dockerContainerName;
    private final String token;
    private final Const.FileType extension;
    private boolean ignoreExisting = true;
    private boolean hasFailed = false;

    public BackupTextChannelService(Bot bot, String channelId, Const.BackupFileType format, String localPathToExport, String[] args) {
        this.bot = bot;
        this.args = args;
        this.channelId = channelId;
        this.token = bot.getBotSettings().getToken();
        this.format = format;
        this.extension = format.getFileType();
        this.localPathToExport = localPathToExport + "text/"; /* Always moving text backups to separate folder */
        this.dockerPathToExport = bot.getBotSettings().getDockerPathToExport();
        this.dockerContainerName = bot.getBotSettings().getDockerContainerName();
    }

    @Override
    public void run() {
        try {
            FileUtils.createFolderIfAbsent(localPathToExport);

            bot.setLockedBackup(true);
            processArguments(args);

            backupFile = FileUtils.getFileByChannelIdAndExtension(localPathToExport, channelId, extension);

            /* If file is present or was made less than 24 hours ago - exit */
            if ((backupFile != null && ((System.currentTimeMillis() - backupFile.lastModified()) < Const.DAY_IN_MILLISECONDS))
                    && ignoreExisting) {
                log.info("Text backup has already been made [{}].", backupFile.getAbsolutePath());
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

        } catch (ParseException | InvalidParameterException | IndexOutOfBoundsException e) {
            failMessage = String.format("Failed to processes provided arguments: [%1$s].", Arrays.toString(args));
            log.error(failMessage);
            hasFailed = true;
        } catch (IOException ioe) {
            failMessage = String.format("Failed to find exported file [%1$s].", ioe.getLocalizedMessage());
            log.error(failMessage);
            hasFailed = true;
        } catch (InterruptedException ie) {
            failMessage = String.format("Backup thread interrupted on services level [%1$s].", ie.getLocalizedMessage());
            log.error(failMessage);
            hasFailed = true;
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

    private void processArguments(String[] args) throws InvalidParameterException, ParseException {
        if (args.length == 0) {
            return;
        }

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-b":
                case "-before":
                    if (validateDateFormat(args[i + 1])) {
                        beforeDate = (args[i + 1]);
                    } else {
                        throw new InvalidParameterException();
                    }
                    break;
                case "-a":
                case "--after":
                    if (validateDateFormat(args[i + 1])) {
                        afterDate = (args[i + 1]);
                    } else {
                        throw new InvalidParameterException();
                    }
                    break;
                case "-f":
                case "--force":

                    /* If force is specified - ignore existing files  */
                    ignoreExisting = false;
                    break;
            }
        }

        /* Check if dates are within correct period (if "before" is more than "after" date) */
        if (beforeDate != null && afterDate != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

            Date before = simpleDateFormat.parse(beforeDate);
            Date after = simpleDateFormat.parse(afterDate);
            if (before.compareTo(after) < 0 || before.compareTo(after) == 0) {
                throw new InvalidParameterException();
            }
        }
    }

    private boolean validateDateFormat(String date) {
        return date.matches("([0-9]{2})/([0-9]{2})/([0-9]{4})");
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
