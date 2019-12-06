package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.services.processes.DockerCallProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class DockerVerificationService {
    private static final Logger log = LoggerFactory.getLogger(RotatingMediaBackupDaemon.class);

    public DockerVerificationService() {}

    public boolean isDockerRunning() {
        try {
            new DockerCallProcess(constructDockerVCommand());
            return true;
        } catch (IOException e) {
            log.warn("Docker verification service has failed: {}", e.getLocalizedMessage());
            return false;
        } catch (InterruptedException e) {
            log.error("Docker verification service was interrupted", e);
            return false;
        }
    }

    private List<String> constructDockerVCommand() {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("-v");

        return command;
    }
}
