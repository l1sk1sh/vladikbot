package com.l1sk1sh.vladikbot.services.processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Oliver Johnson
 */
public class CleanDockerContainerProcess implements Callable<Integer> {
    private static final Logger log = LoggerFactory.getLogger(CleanDockerContainerProcess.class);
    private final List<String> command;

    public CleanDockerContainerProcess(String dockerContainerName) {
        command = new ArrayList<>();
        command.add("docker");
        command.add("rm");
        command.add(dockerContainerName);
    }

    @Override
    public Integer call() throws IOException, InterruptedException {
        log.debug("Running cleaning docker process with command '{}'...", command);
        int exitCode;
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.command(command);

        final Process process = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            log.debug(line);

            if (line.contains("No such container")) {
                return 2;
            } else if (line.contains("Error")) {
                throw new IOException(line);
            }
        }

        exitCode = process.waitFor();
        return exitCode;
    }
}
