package com.l1sk1sh.vladikbot.services.backup.processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Oliver Johnson
 */
public class DockerCallProcess implements Callable<Integer> {
    private static final Logger log = LoggerFactory.getLogger(DockerCallProcess.class);
    private final List<String> command;

    public DockerCallProcess() {
        command = new ArrayList<>();
        command.add("docker");
        command.add("ps");
    }

    @Override
    public Integer call() throws IOException, InterruptedException {
        log.debug("DockerCall receives command '{}'...", command);
        int exitCode;
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.command(command);

        final Process process = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            log.debug(line);

            if (line.contains("command not found")) {
                return 2;
            } else if (line.contains("Cannot connect to the Docker daemon")) {
                return 3;
            }
        }

        exitCode = process.waitFor();
        return exitCode;
    }
}
