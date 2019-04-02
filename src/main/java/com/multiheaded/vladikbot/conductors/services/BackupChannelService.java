package com.multiheaded.vladikbot.conductors.services;

import com.multiheaded.vladikbot.conductors.services.processes.BackupProcess;
import com.multiheaded.vladikbot.conductors.services.processes.CleanProcess;
import com.multiheaded.vladikbot.conductors.services.processes.CopyProcess;
import com.multiheaded.vladikbot.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.multiheaded.vladikbot.settings.Constants.FORMAT_EXTENSION;

/**
 * @author Oliver Johnson
 */
public class BackupChannelService {
    private static final Logger logger = LoggerFactory.getLogger(BackupChannelService.class);

    private File exportedFile;
    private final String channelId;
    private final String format;
    private String beforeDate;
    private String afterDate;
    private final String[] args;
    private final String localPathToExport;
    private final String dockerPathToExport;
    private final String dockerContainerName;
    private final String token;

    public BackupChannelService(String channelId, String format, String[] args,
                                String localPathToExport, String dockerPathToExport,
                                String dockerContainerName, String token)
            throws InvalidParameterException, InterruptedException, IOException {
        this.channelId = channelId;
        this.format = format;
        this.args = args;
        this.localPathToExport = localPathToExport;
        this.dockerPathToExport = dockerPathToExport;
        this.dockerContainerName = dockerContainerName;
        this.token = token;
        String extension = FORMAT_EXTENSION.get(format);

        try {
            String pathToFile = localPathToExport + dockerPathToExport;
            processArguments();

            BackupProcess backupProcess = new BackupProcess(constructBackupCommand());
            logger.info("Waiting for backup to finish...");
            backupProcess.getThread().join();
            if (backupProcess.isFailed()) throw new InterruptedException("BackupProcess failed!");

            FileUtils.deleteFilesByIdAndExtension(pathToFile, channelId, extension);
            CopyProcess copyProcess = new CopyProcess(constructCopyCommand());
            logger.info("Copying received file...");
            copyProcess.getThread().join();
            if (copyProcess.isFailed()) throw new InterruptedException("CopyProcess failed!");

            exportedFile = FileUtils.getFileByIdAndExtension(pathToFile, channelId, extension);
        } catch (IOException ioe) {
            String msg = String.format("Failed to find exported file [%s]", ioe.getMessage());
            logger.error(msg);
            throw new IOException(msg);
        } catch (InterruptedException ie) {
            String msg = String.format("Backup thread interrupted on services level [%s]", ie.getMessage());
            logger.error(msg);
            throw new InterruptedException(msg);
        } finally {
            logger.info("Cleaning docker container...");
            new CleanProcess(constructCleanCommand());
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
        command.add(format);
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
        command.add(dockerContainerName + ":" + dockerPathToExport);
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

    private void processArguments() throws InvalidParameterException {
        try {
            if (args.length > 0) {
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
                    }
                }
            }

            // Check if dates are within correct period (if "before" is more than "after" date)
            if (beforeDate != null && afterDate != null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

                Date before = simpleDateFormat.parse(beforeDate);
                Date after = simpleDateFormat.parse(afterDate);
                if (before.compareTo(after) < 0 || before.compareTo(after) == 0) {
                    throw new InvalidParameterException();
                }
            }
        } catch (ParseException | InvalidParameterException | IndexOutOfBoundsException e) {
            String msg = String.format("Failed to processes provided arguments: %s", Arrays.toString(args));
            logger.error(msg);
            throw new InvalidParameterException(msg);
        }
    }

    private boolean validateDateFormat(String date) {
        return date.matches("([0-9]{2})/([0-9]{2})/([0-9]{4})");
    }

    public File getExportedFile() {
        return exportedFile;
    }
}