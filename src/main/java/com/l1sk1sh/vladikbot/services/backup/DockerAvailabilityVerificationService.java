package com.l1sk1sh.vladikbot.services.backup;

import com.l1sk1sh.vladikbot.services.backup.processes.DockerCallProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Oliver Johnson
 */
public class DockerAvailabilityVerificationService {
    private static final Logger log = LoggerFactory.getLogger(DockerAvailabilityVerificationService.class);

    public DockerAvailabilityVerificationService() {}

    public boolean isDockerRunning() {
        try {
            log.info("Running docker version command...");
            DockerCallProcess dockerCallProcess = new DockerCallProcess();
            int exitCode = dockerCallProcess.call();
            switch (exitCode) {
                case 0:
                    log.debug("Docker is running and ready.");
                    return true;
                case 2:
                    log.debug("Docker command was not found on device.");
                    return false;
                case 3:
                    log.debug("Docker command was found, but daemon is not running.");
                    return false;
                default:
                    log.warn("Unhandled exit code for docker version command: {}.", exitCode);
                    return false;
            }
        } catch (IOException e) {
            log.warn("Docker verification service has failed: {}", e.getLocalizedMessage());
            return false;
        } catch (InterruptedException e) {
            log.error("Docker verification service was interrupted:", e);
            return false;
        }
    }
}
