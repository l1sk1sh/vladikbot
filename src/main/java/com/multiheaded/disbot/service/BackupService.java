package com.multiheaded.disbot.service;

import com.multiheaded.disbot.process.BackupProcess;
import com.multiheaded.disbot.process.CleanProcess;
import com.multiheaded.disbot.process.CopyProcess;
import com.multiheaded.disbot.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.multiheaded.disbot.DisBot.settings;
import static com.multiheaded.disbot.settings.Constants.FORMAT_EXTENSION;

public class BackupService {
    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);

    private boolean completed = false;
    private String channelId;
    private String format;
    private String beforeDate = null;
    private String afterDate = null;
    private String[] args;

    public BackupService(String channelId, String format, String[] args) {
        this.channelId = channelId;
        this.format = format;
        this.args = args;
        String extension = FORMAT_EXTENSION.get(format);

        processArguments();

        try {
            BackupProcess bp = new BackupProcess(constructBackupCommand());
            bp.getThread().join();

            if (bp.isCompleted()) {
                FileUtils.deleteFilesByIdAndExtension(
                        settings.localPathToExport + settings.dockerPathToExport,
                        channelId,
                        extension);
                CopyProcess cp = new CopyProcess(constructCopyCommand());
                cp.getThread().join();
                completed = cp.isCompleted();
            }
        } catch (Exception error) {
            logger.error("Backup thread interrupted on service level.", error.getCause());
        } finally {
            new CleanProcess(constructCleanCommand());
        }
    }

    private List<String> constructBackupCommand() {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("run");
        command.add("--name");
        command.add(settings.dockerContainerName);
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
        command.add(settings.token);
        command.add("--bot");
        command.add("true");

        return command;
    }

    private List<String> constructCopyCommand() {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("cp");
        command.add(settings.dockerContainerName + ":" + settings.dockerPathToExport);
        command.add(settings.localPathToExport);

        return command;
    }

    private List<String> constructCleanCommand() {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("rm");
        command.add(settings.dockerContainerName);

        return command;
    }

    private void processArguments() {
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-b":
                    case "-before":
                        beforeDate = (validateDateFormat(args[i + 1])) ? args[i + 1] : null;
                        break;
                    case "-a":
                    case "--after":
                        afterDate = (validateDateFormat(args[i + 1])) ? args[i + 1] : null;
                        break;
                }
            }
        }

        // Check if dates are within correct period (if "before" is more than "after" date)
        if (beforeDate != null && afterDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

            try {
                Date before = sdf.parse(beforeDate);
                Date after = sdf.parse(afterDate);
                if (before.compareTo(after) < 0 || before.compareTo(after) == 0) {
                    logger.warn("Invalid date provided. Using no-period command");
                    beforeDate = null;
                    afterDate = null;
                }
            } catch (ParseException pe) {
                logger.error("Failed to parse provided dates\n\t-before date: %s\n\t-after date: %s",
                        beforeDate, afterDate);
                beforeDate = null;
                afterDate = null;
            }
        }
    }

    private boolean validateDateFormat(String date) {
        return date.matches("([0-9]{2})/([0-9]{2})/([0-9]{4})");
    }

    public boolean isCompleted() {
        return completed;
    }
}
