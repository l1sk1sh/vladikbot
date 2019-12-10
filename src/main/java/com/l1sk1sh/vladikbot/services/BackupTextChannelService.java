package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.services.processes.BackupProcess;
import com.l1sk1sh.vladikbot.services.processes.CleanProcess;
import com.l1sk1sh.vladikbot.services.processes.CopyProcess;
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
                log.info("Text backup has already been made [{}]", backupFile.getAbsolutePath());
                return;
            }

            log.info("Creating new backup for channel with ID {}", channelId);

            log.info("Clearing docker container before execution...");
            log.debug("CleanProcess receives command {}", constructCleanCommand());
            try {
                new CleanProcess(constructCleanCommand());
                log.info("Container was running and it was cleared.");
            } catch (IllegalStateException notFound) {
                log.info("There was no docker container found.");
            }

            log.info("Waiting for backup to finish...");
            log.debug("BackupProcess receives command {}", constructBackupCommand());
            new BackupProcess(constructBackupCommand());

            FileUtils.getFileByChannelIdAndExtension(localPathToExport, channelId, extension);
            log.info("Copying received file...");
            log.debug("CopyProcess receives command {}", constructCopyCommand());
            new CopyProcess(constructCopyCommand());

            backupFile = FileUtils.getFileByChannelIdAndExtension(localPathToExport, channelId, extension);
            if (backupFile == null) {
                throw new FileNotFoundException("Failed to find or create backup of a channel");
            }

            log.debug("Text Channel Backup Service has finished its execution.");

        } catch (ParseException | InvalidParameterException | IndexOutOfBoundsException e) {
            failMessage = String.format("Failed to processes provided arguments: %1$s", Arrays.toString(args));
            log.error(failMessage);
            hasFailed = true;
        } catch (IOException ioe) {
            failMessage = String.format("Failed to find exported file [%1$s]", ioe.getLocalizedMessage());
            log.error(failMessage);
            hasFailed = true;
        } catch (InterruptedException ie) {
            failMessage = String.format("Backup thread interrupted on services level [%1$s]", ie.getLocalizedMessage());
            log.error(failMessage);
            hasFailed = true;
        } finally {
            try {
                log.info("Cleaning docker container after execution...");
                log.debug("Final CleanProcess receives command {}", constructCleanCommand());
                new CleanProcess(constructCleanCommand());
            } catch (InterruptedException ire) {
                log.error("Clean process thread was interrupted {}", ire.getLocalizedMessage());
            } catch (IOException ioe) {
                log.error("Cleaning failed due to IO", ioe);
            } catch (IllegalStateException notFound) {
                log.warn("Container for final cleaning was not found");
            } finally {
                bot.setLockedBackup(false);
            }
        }
    }

    private List<String> constructBackupCommand() {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("run");
        command.add("--name");
        command.add(dockerContainerName);
        command.add("tyrrrz/discordchatexporter");
        command.add("export");
        command.add("-f");
        command.add(format.getBackupTypeName());
        if (beforeDate != null) {
            command.add("--before");
            command.add(beforeDate);
        }
        if (afterDate != null) {
            command.add("--after");
            command.add(afterDate);
        }
        command.add("--channel");
        command.add(channelId);
        command.add("--token");
        command.add(token);
        command.add("--bot");
        command.add("true");

        return command;
    }

    private List<String> constructCopyCommand() {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("cp");
        command.add(dockerContainerName + ":" + dockerPathToExport + ".");
        command.add(localPathToExport);

        return command;
    }

    private List<String> constructCleanCommand() {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("rm");
        command.add(dockerContainerName);

        return command;
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
