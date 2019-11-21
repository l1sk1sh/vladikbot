package l1.multiheaded.vladikbot.services;

import l1.multiheaded.vladikbot.models.LockService;
import l1.multiheaded.vladikbot.services.processes.BackupProcess;
import l1.multiheaded.vladikbot.services.processes.CleanProcess;
import l1.multiheaded.vladikbot.services.processes.CopyProcess;
import l1.multiheaded.vladikbot.settings.Constants;
import l1.multiheaded.vladikbot.utils.FileUtils;
import org.omg.CosNaming.NamingContextPackage.NotFound;
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
public class BackupChannelService {
    private static final Logger logger = LoggerFactory.getLogger(BackupChannelService.class);

    private File exportedFile;
    private final String channelId;
    private final String format;
    private String beforeDate;
    private String afterDate;
    private final String localPathToExport;
    private final String dockerPathToExport;
    private final String dockerContainerName;
    private final String token;
    private boolean forceBackup = false;

    public BackupChannelService(String channelId, String token, String format,
                                String localPathToExport, String dockerPathToExport,
                                String dockerContainerName, String[] args, LockService lock)
            throws InvalidParameterException, InterruptedException, IOException {

        this.channelId = channelId;
        this.format = format;
        this.localPathToExport = localPathToExport;
        this.dockerPathToExport = dockerPathToExport;
        this.dockerContainerName = dockerContainerName;
        this.token = token;
        String extension = Constants.FORMAT_EXTENSION.get(format);

        try {
            lock.setLocked(true);
            processArguments(args);

            exportedFile = FileUtils.getFileByIdAndExtension(localPathToExport, channelId, extension);

            /* If file is absent or was made more than 24 hours ago - create new backup */
            if ((exportedFile == null)
                    || ((System.currentTimeMillis() - exportedFile.lastModified()) > Constants.DAY_IN_MILLISECONDS)
                    || forceBackup) {

                logger.info("Clearing docker container before launch...");
                logger.debug("Passing command {}", constructCleanCommand());
                try {
                    new CleanProcess(constructCleanCommand());
                    logger.info("Container was running and it was cleared.");
                } catch (NotFound notFound) {
                    logger.info("There was no docker container found.");
                }

                logger.info("Waiting for backup to finish...");
                logger.debug("Passing command {}", constructBackupCommand());
                new BackupProcess(constructBackupCommand());

                FileUtils.deleteFilesByIdAndExtension(localPathToExport, channelId, extension);
                logger.info("Copying received file...");
                logger.debug("Passing command {}", constructCopyCommand());
                new CopyProcess(constructCopyCommand());

                exportedFile = FileUtils.getFileByIdAndExtension(localPathToExport, channelId, extension);
                if (exportedFile == null) {
                    throw new FileNotFoundException("Failed to find or create backup of a channel");
                }
            }
        } catch (IOException ioe) {
            String msg = String.format("Failed to find exported file [%s]", ioe.getLocalizedMessage());
            logger.error(msg);
            throw new IOException(msg);
        } catch (InterruptedException ie) {
            String msg = String.format("Backup thread interrupted on services level [%s]", ie.getLocalizedMessage());
            logger.error(msg);
            throw new InterruptedException(msg);
        } finally {
            try {
                logger.info("Cleaning docker container...");
                logger.debug("Passing command {}", constructCleanCommand());
                new CleanProcess(constructCleanCommand());
            } catch (InterruptedException ire) {
                logger.error("Clean process thread was interrupted {}", ire.getLocalizedMessage());
            } catch (NotFound nf) {
                logger.error("Container was not found");
            } finally {
                lock.setLocked(false);
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

    private void processArguments(String[] args) throws InvalidParameterException {
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
                        case "-f":
                        case "--force":
                            forceBackup = true;
                            break;
                    }
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
