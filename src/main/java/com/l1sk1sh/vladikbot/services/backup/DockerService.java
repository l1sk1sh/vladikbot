package com.l1sk1sh.vladikbot.services.backup;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.settings.Const;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Oliver Johnson
 */
public class DockerService {
    private static final Logger log = LoggerFactory.getLogger(DockerService.class);

    private static final String DOCKER_IMAGE = "tyrrrz/discordchatexporter";
    private static final String CONTAINER_NAME = "disbackup";
    private static final String CONTAINER_WORKDIR_COPY = ".";
    private static final String CONTAINER_WORKDIR = "/app/out/" + CONTAINER_WORKDIR_COPY;

    private DockerClient docker;
    private Container backupContainer;
    private final List<String> logs = new ArrayList<>();

    public DockerService(Bot bot) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(bot.getBotSettings().getDockerHost())
                .build();

        this.docker = DockerClientBuilder.getInstance(config)
                .build();
    }

    public boolean isDockerRunning() {
        if (!infoCommand()) {
            return false;
        }

        return pullImage();
    }

    boolean runBackup(Const.BackupFileType format, String beforeDate, String afterDate,
                      String channelId, String token) {
        List<String> command = new ArrayList<>();
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

        log.trace("Running export command: {}", command.toString());
        return runContainerWithCommand(command.toArray(new String[0]));
    }

    public List<String> getContainerLogs() {
        return logs;
    }

    private void recordLastContainerLogs() {
        if (backupContainer == null) {
            log.warn("Calling docker logs foe empty container.");

            return;
        }

        logs.clear();

        LogContainerCmd logContainerCmd = docker.logContainerCmd(backupContainer.getId());
        logContainerCmd.withStdOut(true).withStdErr(true);
        // logContainerCmd.withSince( lastLogTime );  // UNIX timestamp (integer) to filter logs. Specifying a timestamp will only output log-entries since that timestamp.
        logContainerCmd.withTail(10);  // Get only last 10 records
        logContainerCmd.withTimestamps(true);
        logContainerCmd.withFollowStream(true);

        try {
            //noinspection deprecation
            logContainerCmd.exec(new LogContainerResultCallback() {
                @Override
                public void onNext(Frame item) {
                    logs.add(item.toString());
                }
            }).awaitCompletion();
        } catch (InterruptedException e) {
            log.error("Docker logs command was interrupted: '{}'.", e.getMessage());
        }
    }

    private boolean runContainerWithCommand(String... command) {
        try {
            removeContainerIfExists();
        } catch (ConflictException e) {
            log.warn("Removal of container is already in progress.");
        }

        if (!createContainerWithCommand(command)) {
            log.error("Failed to create container.");

            return false;
        }

        backupContainer = findContainer();

        if (backupContainer == null) {
            log.error("Container created but still absent?..");

            return false;
        }

        docker.startContainerCmd(backupContainer.getId()).exec();

        WaitContainerResultCallback resultCallback = new WaitContainerResultCallback();
        docker.waitContainerCmd(backupContainer.getId()).exec(resultCallback);

        try {
            resultCallback.awaitCompletion();
        } catch (InterruptedException e) {
            log.error("Failed to get container response: {}", e.getLocalizedMessage());

            return false;
        } finally {
            recordLastContainerLogs();
        }

        return true;
    }

    private void removeContainerIfExists() throws ConflictException {
        backupContainer = findContainer();

        if (backupContainer != null) {
            docker.removeContainerCmd(backupContainer.getId())
                    .withForce(true)
                    .exec();
        }
    }

    private Container findContainer() {
        List<Container> containers = docker.listContainersCmd()
                .withNameFilter(Collections.singletonList(CONTAINER_NAME))
                .withShowAll(true)
                .exec();

        if (containers.size() > 0) {
            return containers.get(0);
        }

        return null;
    }

    File copyBackupFile(String targetDirectory) {
        backupContainer = findContainer();

        if (backupContainer == null) {
            log.error("Container is already dead. Cannot copy.");

            return null;
        }

        try (InputStream inputStream = docker.copyArchiveFromContainerCmd(backupContainer.getId(), CONTAINER_WORKDIR).exec();
             TarArchiveInputStream tarInputStream = new TarArchiveInputStream(inputStream)) {

            File backupFile = null;
            ArchiveEntry nextEntry;
            while ((nextEntry = tarInputStream.getNextEntry()) != null) {
                if (nextEntry.getName().equals(CONTAINER_WORKDIR_COPY + "/")) {
                    continue;
                }

                backupFile = new File(targetDirectory + nextEntry.getName().replace(CONTAINER_WORKDIR_COPY + "/", ""));
                Files.copy(tarInputStream, backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                break;
            }

            return backupFile;
        } catch (IOException e) {
            log.error("Failed to copy backup file: ", e);
        }

        return null;
    }

    private boolean createContainerWithCommand(String... command) {
        try {
            docker.createContainerCmd(DOCKER_IMAGE)
                    .withName(CONTAINER_NAME)
                    .withCmd(command)
                    .exec();

            return true;
        } catch (NotFoundException e) {
            log.error("Image {} not found. [{}]", DOCKER_IMAGE, e.getLocalizedMessage());
            return false;
        } catch (ConflictException e) {
            log.error("Container from image {} already exists. [{}]", DOCKER_IMAGE, e.getLocalizedMessage());
            return false;
        }
    }

    private boolean pullImage() {
        try {
            return docker.pullImageCmd(DOCKER_IMAGE)
                    .start()
                    .awaitCompletion(60, TimeUnit.SECONDS);
        } catch (RuntimeException e) {
            log.debug("Failed to pull image.", e);
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

    private boolean infoCommand() {
        try {
            docker.infoCmd().exec();

            return true;
        } catch (ProcessingException e) {
            log.debug("Docker info command has failed: {}", e.getLocalizedMessage());

            return false;
        }
    }
}
